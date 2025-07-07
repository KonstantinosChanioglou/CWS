# Use official lightweight Python image
FROM python:3.10-slim

# Set working directory
WORKDIR /app

# Install curl and cleanup
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy the necessary files
COPY requirements.txt .
COPY pm1.py .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Expose the port your app runs on
EXPOSE 8090

# Run the app
CMD ["python", "pm1.py"]
