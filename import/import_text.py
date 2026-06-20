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
    "id": "IMPORT",
    "password": "oi4PwZAxWqkSLQ",
    "email": "import@eyetap.ch",
    "accountType": "SURVEY_ADMIN"
}

import json
import math


def find_nans(obj, path="root"):
    """
    Recursively locate NaN values inside nested dict/list structures.
    """
    if isinstance(obj, dict):
        for k, v in obj.items():
            find_nans(v, f"{path}.{k}")
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            find_nans(v, f"{path}[{i}]")
    elif isinstance(obj, float) and math.isnan(obj):
        print(f"NaN found at {path}")


def print_nan_rows(df, name):
    nan_rows = df[df.isna().any(axis=1)]

    if len(nan_rows) > 0:
        print(f"\n===== NaN rows found in {name} =====")
        print(nan_rows.to_string())
        print("====================================\n")
    else:
        print(f"No NaN rows found in {name}")


def validate_group(text_id, lang, text_group, char_group):
    print(f"\n--- Validating {text_id}-{lang} ---")

    print_nan_rows(text_group, f"text_group ({text_id}-{lang})")
    print_nan_rows(char_group, f"char_group ({text_id}-{lang})")

    coord_cols = ["x_min", "x_max", "y_min", "y_max"]

    for col in coord_cols:
        if col in text_group.columns:
            bad = text_group[text_group[col].isna()]
            if not bad.empty:
                print(f"\nMissing {col} in text_group:")
                print(bad.to_string())

        if col in char_group.columns:
            bad = char_group[char_group[col].isna()]
            if not bad.empty:
                print(f"\nMissing {col} in char_group:")
                print(bad.to_string())


def safe_float(value):
    if pd.isna(value):
        return None
    return float(value)


def safe_int(value):
    if pd.isna(value):
        return None
    return int(value)


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
            "foreignId": safe_int(row["word_id"]),
            "word": row["word_text"],
            "xMin": safe_float(row["x_min"]),
            "xMax": safe_float(row["x_max"]),
            "yMin": safe_float(row["y_min"]),
            "yMax": safe_float(row["y_max"])
        }
        for _, row in text_group.iterrows()
    ]

    char_boxes = [
        {
            "foreignId": safe_int(row["char_uid"]),
            "character": row["char_text"],
            "xMin": safe_float(row["x_min"]),
            "xMax": safe_float(row["x_max"]),
            "yMin": safe_float(row["y_min"]),
            "yMax": safe_float(row["y_max"])
        }
        for _, row in char_group.iterrows()
    ]

    dto = {
        "title": title,
        "foreignId": text_id,
        "language": language,
        "characterBoundingBoxes": char_boxes,
        "wordBoundingBoxes": word_boxes,
        "backgroundImage": encode_background_image_for_text(text_id, language)
    }

    return dto


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

        print(f"\nProcessing text {text_id}-{lang}...")

        text_uids = text_group["text_uid"].unique()
        char_group = char_df[char_df["text_uid"].isin(text_uids)]

        validate_group(text_id, lang, text_group, char_group)

        import_text_dto = build_import_text_dto(text_group, char_group)

        print("Checking DTO for NaN values...")
        find_nans(import_text_dto)

        try:
            json.dumps(import_text_dto, allow_nan=False)
            print("DTO JSON validation passed.")
        except ValueError as e:
            print(f"\nJSON validation failed for {text_id}-{lang}")
            print(e)

            # print("\nDumping DTO structure:")
            # print(json.dumps(import_text_dto, indent=2, default=str))

            continue

        try:
            response = post_json(IMPORT_TEXT_ENDPOINT, import_text_dto)
            print(f"Imported text {text_id}-{lang}: {response}")

        except requests.HTTPError as e:
            if e.response.status_code == 409:
                print(f"Text {text_id}-{lang} already exists. Skipping.")
            else:
                print(f"HTTP error for {text_id}-{lang}:")
                print(e.response.text)
                raise

        except Exception as e:
            print(f"Unexpected error for {text_id}-{lang}: {e}")
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
