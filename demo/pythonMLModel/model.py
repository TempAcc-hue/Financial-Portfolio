import torch
from torch import nn
import pandas as pd
import numpy as np
from sklearn.preprocessing import MinMaxScaler

# --------------------------
# 1. Recreate the model class
# --------------------------
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

# --------------------------
# 2. Load model weights
# --------------------------
model = LSTMModel()
model.load_state_dict(torch.load("lstm_stock_model.pth", map_location="cpu"))
model.eval()

print("Model loaded.")

# Load your data
# df = pd.read_csv("your_stock_data.csv")

# df = df[['Open', 'High', 'Low', 'Close', 'Volume']]

# # Scale again using MinMaxScaler
# scaler = MinMaxScaler()
# scaled = scaler.fit_transform(df)

# # Use last 60 days for prediction
# seq_len = 60
# last_seq = scaled[-seq_len:]   # shape = (60, 5)

# x_input = torch.tensor(last_seq, dtype=torch.float32).unsqueeze(0)  # (1,60,5)

# pred_scaled = model(x_input).item()

# # inverse scale (only Close column)
# dummy = np.zeros((1,5))
# dummy[:,3] = pred_scaled

# pred_price = scaler.inverse_transform(dummy)[0,3]

# print(f"Next predicted closing price: ${pred_price:.2f}")
