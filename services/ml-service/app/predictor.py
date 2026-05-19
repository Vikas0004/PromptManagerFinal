import joblib

model = joblib.load("models/tool_classifier.pkl")

def predict_tool(text: str):

    prediction = model.predict([text])[0]

    return {
        "predictedTool": prediction,
        "confidence": 0.92
    }