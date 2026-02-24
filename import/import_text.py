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

TEXT_IMAGE_FOLDER = os.path.join(DATA_SOURCE, "texts")  # e.g., data/texts

# =========================
# HELPERS
# =========================


def encode_background_image_for_text(text_id, language):
    """
    Looks for a file Item_{text_id}.png in the correct language folder.
    Returns base64 string or None if file does not exist.
    """
    folder_path = os.path.join(TEXT_IMAGE_FOLDER, language)
    image_path = os.path.join(folder_path, f"Item_{text_id}.png")
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

# =========================
# BUILD IMPORT TEXT DTO
# =========================

def build_import_text_dto(text_group, char_group):
    # Take text_id and lang from the group
    text_id = int(text_group.iloc[0]["text_id"])
    language = text_group.iloc[0]["lang"]

    title = f"Text_{text_id}_{language}"

    # Word bounding boxes
    word_boxes = []
    for _, row in text_group.iterrows():
        word_boxes.append({
            "foreignId": int(row["word_id"]),
            "word": row["word_text"],
            "xMin": float(row["x_min"]),
            "xMax": float(row["x_max"]),
            "yMin": float(row["y_min"]),
            "yMax": float(row["y_max"])
        })

    # Character bounding boxes
    char_boxes = []
    for _, row in char_group.iterrows():
        char_boxes.append({
            "foreignId": int(row["char_uid"]),
            "character": row["char_text"],  # include if supported
            "xMin": float(row["x_min"]),
            "xMax": float(row["x_max"]),
            "yMin": float(row["y_min"]),
            "yMax": float(row["y_max"])
        })

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

def main():
    print("Loading CSV files...")

    text_df = pd.read_csv(TEXT_CSV)
    char_df = pd.read_csv(CHAR_CSV)

    print("Importing texts...")

    # Group texts by unique identifier: text_id + lang
    for (text_id, lang), text_group in text_df.groupby(["text_id", "lang"]):
        print(f"Processing text {text_id}-{lang}...")

        # Collect all text_uids in this group
        text_uids = text_group["text_uid"].unique()

        # Filter characters for all text_uids
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
    main()