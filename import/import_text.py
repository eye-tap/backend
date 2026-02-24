import argparse
import base64
import os
import time

import pandas as pd
import requests

# =========================
# CONFIGURATION
# =========================

BASE_URL = "http://localhost:8080"
IMPORT_TEXT_ENDPOINT = f"{BASE_URL}/import/text"
DATA_SOURCE = "data/"

TEXT_CSV = f"{DATA_SOURCE}texts.csv"
CHAR_CSV = f"{DATA_SOURCE}characters.csv"

TEXT_IMAGE_FOLDER = os.path.join(DATA_SOURCE, "texts")  # e.g., data/texts

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
        time.sleep(1)
        response = requests.post(LOGIN_URL, json={"id": credentials["id"], "password": credentials["password"]})
        response.raise_for_status()
    token = response.json()["token"]
    print(f"JWT: {token}")
    return token

TOKEN = get_jwt_token()

# =========================
# HELPERS
# =========================

def encode_background_image_for_text(text_id, language):
    folder_path = os.path.join(TEXT_IMAGE_FOLDER, language)
    print(f"Trying to resolve Item_{"{:02d}".format(text_id)}.png")
    image_path = os.path.join(folder_path, f"Item_{"{:02d}".format(text_id)}.png")
    print(f"Image path {image_path}")
    if not os.path.exists(image_path):
        return None
    with open(image_path, "rb") as img_file:
        return base64.b64encode(img_file.read()).decode("utf-8")

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

def build_import_text_dto(text_group, char_group):
    text_id = int(text_group.iloc[0]["text_id"])
    language = text_group.iloc[0]["lang"]

    title = f"Text_{text_id}_{language}"

    word_boxes = [
        {
            "foreignId": int(row["word_id"]),
            "word": row["word_text"],
            "xMin": float(row["x_min"]),
            "xMax": float(row["x_max"]),
            "yMin": float(row["y_min"]),
            "yMax": float(row["y_max"])
        }
        for _, row in text_group.iterrows()
    ]

    char_boxes = [
        {
            "foreignId": int(row["char_uid"]),
            "character": row["char_text"],
            "xMin": float(row["x_min"]),
            "xMax": float(row["x_max"]),
            "yMin": float(row["y_min"]),
            "yMax": float(row["y_max"])
        }
        for _, row in char_group.iterrows()
    ]

    return {
        "title": title,
        "foreignId": text_id,
        "language": language,
        "characterBoundingBoxes": char_boxes,
        "wordBoundingBoxes": word_boxes,
        "backgroundImage": encode_background_image_for_text(text_id, language)
    }

# =========================
# MAIN EXECUTION
# =========================

def main(languages_to_import=None):
    print("Loading CSV files...")

    text_df = pd.read_csv(TEXT_CSV)
    char_df = pd.read_csv(CHAR_CSV)

    print("Importing texts...")

    for (text_id, lang), text_group in text_df.groupby(["text_id", "lang"]):
        if languages_to_import and lang not in languages_to_import:
            print(f"Skipping text {text_id}-{lang} due to language filter")
            continue

        print(f"Processing text {text_id}-{lang}...")
        text_uids = text_group["text_uid"].unique()
        char_group = char_df[char_df["text_uid"].isin(text_uids)]

        import_text_dto = build_import_text_dto(text_group, char_group)

        try:
            response = post_json(IMPORT_TEXT_ENDPOINT, import_text_dto)
            print(f"Imported text {text_id}-{lang}: {response}")
        except requests.HTTPError as e:
            if e.response.status_code == 409:
                print(f"Text {text_id}-{lang} already exists. Skipping.")
            else:
                raise

    print("Done.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Import texts with optional language filter")
    parser.add_argument(
        "--languages",
        nargs="*",
        help="Languages to import (e.g., --languages en gr). If omitted, all languages are imported."
    )
    args = parser.parse_args()
    languages_set = set(args.languages) if args.languages else None
    main(languages_set)