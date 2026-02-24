# File: import_reading_sessions.py
import time

import pandas as pd
import requests

# =========================
# CONFIGURATION
# =========================

BASE_URL = "http://localhost:8080"
IMPORT_READING_SESSION_ENDPOINT = f"{BASE_URL}/import/reading-session"
DATA_SOURCE = "data/"

FIX_CSV = f"{DATA_SOURCE}fixations.csv"
ANNOT_CSV = f"{DATA_SOURCE}annotations.csv"
CHAR_CSV = f"{DATA_SOURCE}characters.csv"
TEXT_CSV = f"{DATA_SOURCE}texts.csv"

LOGIN_URL = f"{BASE_URL}/auth/login"
REGISTER_URL = f"{BASE_URL}/auth/register"

credentials = {
    "id": "Paul",
    "password": "123456",
    "email": "paul@franos.ch",
    "accountType": "SURVEY_ADMIN"
}

# =========================
# LOGIN / REGISTER
# =========================

def get_jwt_token():
    """Attempt login, if fails, register and retry."""
    try:
        response = requests.post(LOGIN_URL, json={"id": credentials["id"], "password": credentials["password"]})
        response.raise_for_status()
    except requests.HTTPError as e:
        print(f"Login failed: {e}. Attempting registration...")
        reg_body = {
            "id": credentials["id"],
            "email": credentials["email"],
            "password": credentials["password"],
            "accountType": credentials["accountType"]
        }
        reg_resp = requests.post(REGISTER_URL, json=reg_body)
        reg_resp.raise_for_status()
        print("Registration successful. Waiting 1s for account propagation...")
        time.sleep(1)  # small delay to ensure backend is ready
        # Retry login
        response = requests.post(LOGIN_URL, json={"id": credentials["id"], "password": credentials["password"]})
        response.raise_for_status()
    token = response.json()["token"]
    print(f"JWT: {token}")
    return token

TOKEN = get_jwt_token()

# =========================
# HELPERS
# =========================

def post_json(url, payload):
    headers = {
        "Authorization": f"Bearer {TOKEN}",
        "Content-Type": "application/json"
    }
    response = requests.post(url, json=payload, headers=headers)
    if not response.ok:
        print("Request failed:", response.status_code, response.text)
        response.raise_for_status()
    return response.json()

# =========================
# BUILD READING SESSION DTO
# =========================

def build_import_reading_session_dto(fix_df, annot_df, text_id, lang):
    """
    Build ImportReadingSessionDto from fixation and annotation data.
    Ensures all pre-annotations reference valid fixations and characters.
    """
    if fix_df.empty:
        return None

    # Reader ID is sub_id
    reader_id = int(fix_df.iloc[0]["sub_id"])

    # Fixations
    fixations = []
    valid_fix_ids = set()  # track IDs that actually exist
    for _, row in fix_df.iterrows():
        fix_id = int(row["fix_uid"])
        fixations.append({
            "foreignId": fix_id,
            "x": float(row["x"]),
            "y": float(row["y"]),
            "disagreement": float(row["H_vote"]) if not pd.isna(row["H_vote"]) else 0.0
        })
        valid_fix_ids.add(fix_id)

    # Pre-annotations grouped by algorithm_id
    pre_annotations = []

    # Only keep annotation rows where fix_uid exists and char_uid is not NaN
    annot_df_filtered = annot_df[
        annot_df["fix_uid"].isin(valid_fix_ids) &
        annot_df["char_uid"].notna()
    ]

    grouped = annot_df_filtered.groupby("algorithm_id")
    for algorithm_id, group in grouped:
        values = []
        for _, row in group.iterrows():
            values.append({
                "foreignFixationId": int(row["fix_uid"]),
                "foreignCharacterBoxId": int(row["char_uid"]),
                "dGeom": float(row["D_geom"]),
                "pShare": float(row["P_share"])
            })
        pre_annotations.append({
            "title": f"Algorithm_{algorithm_id}",
            "annotations": values
        })

    return {
        "readerForeignId": reader_id,
        "textForeignId": int(text_id),
        "language": lang,
        "fixations": fixations,
        "preAnnotations": pre_annotations
    }

# =========================
# MAIN EXECUTION
# =========================

def main():
    print("Loading CSV files...")
    fix_df = pd.read_csv(FIX_CSV)
    annot_df = pd.read_csv(ANNOT_CSV)

    print("Importing reading sessions...")

    # Group by reader + text + language
    for (sub_id, text_id, lang), fix_group in fix_df.groupby(["sub_id", "text_id", "lang"]):
        print(f"Processing reading session reader={sub_id}, text={text_id}, lang={lang}")

        # Filter annotations for this reader/text
        annot_group = annot_df[annot_df["fix_uid"].isin(fix_group["fix_uid"])]

        reading_session_dto = build_import_reading_session_dto(fix_group, annot_group, text_id, lang)
        if reading_session_dto is None:
            print(f"No fixations for reader={sub_id}, text={text_id}, lang={lang}, skipping.")
            continue

        try:
            response = post_json(IMPORT_READING_SESSION_ENDPOINT, reading_session_dto)
            print(f"Imported reading session for reader={sub_id}, text={text_id}, lang={lang}: {response}")
        except requests.HTTPError as e:
            print(f"Failed to import reading session reader={sub_id}, text={text_id}, lang={lang}")
            raise

    print("Done.")

if __name__ == "__main__":
    main()