from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/temperature_type1', methods=['GET'])
def measureTemperature():
    response = {
        "status": "success",
        "message": "Temperature taking has been finished succesfully at PM!",
        "value": 39  # Example value to use later
    }
    return jsonify(response), 200

@app.route('/respirationrate_type1', methods=['GET'])
def measureRespirationRate():
    response = {
        "status": "success",
        "message": "Respiration Rate taking has been finished succesfully at PM!",
        "value": 95  # Example value to use later
    }
    return jsonify(response), 200

@app.route('/heartrate_type1', methods=['GET'])
def measureHeartRate():
    response = {
        "status": "success",
        "message": "Heart Rate taking has been finished succesfully at PM!",
        "value": 70  # Example value to use later
    }
    return jsonify(response), 200

@app.route('/systolicBloodPressure_type1', methods=['GET'])
def measureSystolicBloodPressure():
    response = {
        "status": "success",
        "message": "Systolic Blood Pressure taking has been finished succesfully at PM!",
        "value": 95  # Example value to use later
    }
    return jsonify(response), 200

@app.route('/meanArterialPressure_type1', methods=['GET'])
def measureMeanArterialPressure():
    response = {
        "status": "success",
        "message": "meanArterialPressure Pressure taking has been finished succesfully at PM!",
        "value": 80  # Example value to use later
    }
    return jsonify(response), 200



@app.route('/receive-task_type1', methods=['POST'])
def receive_task():
    data = request.json  # Extract the JSON payload sent by Flowable Cloud
    print("Received request from Flowable Cloud:", data)

    # Send a simple response back
    response = {
        "status": "received",
        "message": "The request was successfully received.",
        "receivedData": data
    }
    return jsonify(response), 200

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8090)
