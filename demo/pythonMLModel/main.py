import torch
import torch.nn as nn
import numpy as np
import joblib
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List

# ---------------------------------------------------------
# 1. Define Request Body Format
# ---------------------------------------------------------
# We expect a list of data points (Open, High, Low, Close, Volume)
# The length of this list must match the sequence length you trained on (e.g., 60 days)
class StockData(BaseModel):
    data: List[List[float]] # Example: [[150.1, 152.0, ...], [151.0, ...], ...]

# ---------------------------------------------------------
# 2. Recreate Model Class (Must match training exactly)
# ---------------------------------------------------------
class LSTMModel(nn.Module):
    def __init__(self, input_size=5, hidden_size=64, num_layers=2, dropout=0.2):
        super().__init__()
        self.lstm = nn.LSTM(
            input_size=input_size,
            hidden_size=hidden_size,
            num_layers=num_layers,
            dropout=dropout,
            batch_first=True
        )
        self.fc = nn.Linear(hidden_size, 1)

    def forward(self, x):
        out, _ = self.lstm(x)
        out = out[:, -1, :]
        out = self.fc(out)
        return out

# ---------------------------------------------------------
# 3. Initialize App & Load Artifacts
# ---------------------------------------------------------
app = FastAPI()

# Global variables to hold model and scaler
model = None
scaler = None

@app.on_event("startup")
def load_artifacts():
    global model, scaler
    
    # A. Load the Scaler
    try:
        scaler = joblib.load("scaler.gz")
        print("Scaler loaded successfully.")
    except Exception as e:
        print(f"Error loading scaler: {e}")
        # In production, you might want to stop the server here if scaler fails

    # B. Load the Model
    try:
        model = LSTMModel()
        model.load_state_dict(torch.load("lstm_stock_model.pth", map_location="cpu"))
        model.eval() # Set to evaluation mode (turns off dropout)
        print("Model loaded successfully.")
    except Exception as e:
        print(f"Error loading model: {e}")

# ---------------------------------------------------------
# 4. Define Prediction Endpoint
# ---------------------------------------------------------
@app.post("/predict")
def predict_price(stock_input: StockData):
    if not model or not scaler:
        raise HTTPException(status_code=500, detail="Model or Scaler not loaded")

    try:
        # A. Convert input list to numpy array
        raw_data = np.array(stock_input.data) 
        # raw_data shape should be (Sequence_Length, 5)

        # B. Scale the data using the loaded scaler
        # Note: MinMaxScaler expects 2D array
        scaled_data = scaler.transform(raw_data)

        # C. Prepare tensor for PyTorch
        # LSTM expects: (Batch_Size, Sequence_Length, Features)
        # We add a batch dimension of 1
        input_tensor = torch.tensor(scaled_data, dtype=torch.float32).unsqueeze(0)

        # D. Make Prediction
        with torch.no_grad():
            prediction_scaled = model(input_tensor)

        # E. Inverse Transform the result
        # The model outputs a scaled value (e.g., 0.5). We need to convert it back to $$$
        # Inverse transform usually requires the same number of features as input (5).
        # We create a dummy row to perform the inverse transform safely.
        dummy_row = np.zeros((1, 5))
        # Assuming the target (Close price) was at a specific index during scaling (e.g., index 3)
        # If you scaled only the target separately, use that scaler. 
        # For simplicity, we usually create a dummy array where we put our prediction in the correct column.
        
        # HACK: If you scaled all 5 features together, getting the inverse specific to "Close" 
        # requires knowing which column "Close" is. Let's assume it's index 3 (0=Open, 1=High, 2=Low, 3=Close, 4=Vol)
        # You replace the value at index 3 with your prediction.
        
        prediction_value = prediction_scaled.item()
        dummy_row[0, 3] = prediction_value 
        
        inverse_scaled_row = scaler.inverse_transform(dummy_row)
        final_price = inverse_scaled_row[0, 3]

        return {
            "predicted_price": final_price, 
            "raw_scaled_output": prediction_value
        }

    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))