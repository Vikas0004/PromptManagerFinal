import pandas as pd
import joblib

from sklearn.pipeline import Pipeline
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.svm import LinearSVC

df = pd.read_csv("training_data.csv")

X = df["text"]
y = df["tool"]

model = Pipeline([
    ("tfidf", TfidfVectorizer()),
    ("classifier", LinearSVC())
])

model.fit(X, y)

joblib.dump(model, "models/tool_classifier.pkl")

print("Model trained successfully")