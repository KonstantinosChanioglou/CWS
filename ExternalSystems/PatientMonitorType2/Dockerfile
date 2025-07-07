# Use official lightweight Python image
FROM python:3.10-slim

# Set working directory
WORKDIR /app

# Copy the necessary files
COPY requirements.txt .
COPY pm2.py .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Expose the port your app runs on
EXPOSE 8091

# Run the app
CMD ["python", "pm2.py"]
