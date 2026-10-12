#!/usr/bin/env python
# backend/ai/train.py
# Trains the category classifier and saves the model with joblib.
# Run once before starting the server: python ai/train.py

import csv
import joblib
from pathlib import Path
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import cross_val_score
from sklearn.pipeline import Pipeline
import numpy as np

DATA_PATH  = Path(__file__).parent / "data" / "complaints.csv"
MODEL_DIR  = Path(__file__).parent / "models"
MODEL_DIR.mkdir(exist_ok=True)

CAT_MODEL_PATH = MODEL_DIR / "category_model.pkl"
CAT_VEC_PATH   = MODEL_DIR / "category_vectorizer.pkl"


def load_data():
    texts, labels = [], []
    with open(DATA_PATH, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            text = row["text"].strip().strip('"')
            cat  = row["category"].strip()
            if text and cat:
                texts.append(text.lower())
                labels.append(cat)
    return texts, labels


def train():
    print("📂  Loading training data …")
    texts, labels = load_data()
    print(f"    {len(texts)} samples across {len(set(labels))} categories")

    # TF-IDF vectorizer
    vectorizer = TfidfVectorizer(
        ngram_range=(1, 2),
        max_features=5000,
        sublinear_tf=True,
    )
    X = vectorizer.fit_transform(texts)

    # Logistic Regression classifier
    clf = LogisticRegression(max_iter=1000, C=5.0, solver="lbfgs", multi_class="auto")

    # Cross-validation to report accuracy
    scores = cross_val_score(clf, X, labels, cv=5, scoring="accuracy")
    print(f"✅  5-fold CV accuracy: {np.mean(scores):.2%} ± {np.std(scores):.2%}")

    # Train on full dataset
    clf.fit(X, labels)

    # Save models
    joblib.dump(clf,        CAT_MODEL_PATH)
    joblib.dump(vectorizer, CAT_VEC_PATH)
    print(f"💾  Saved model → {CAT_MODEL_PATH}")
    print(f"💾  Saved vectorizer → {CAT_VEC_PATH}")
    print("\nDone! Start the server now.")


if __name__ == "__main__":
    train()
