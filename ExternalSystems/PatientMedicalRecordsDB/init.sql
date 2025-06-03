CREATE TABLE patients (
                          patientId SERIAL PRIMARY KEY,
                          age INT,
                          immune_compromised BOOLEAN,
                          chronic_illness BOOLEAN,
                          recent_surgery BOOLEAN
);

INSERT INTO patients (patientId, age, immune_compromised, chronic_illness, recent_surgery) VALUES
                                                                                    (1,72, true, true, false),
                                                                                    (2,45, false, true, true),
                                                                                    (3,29, false, false, false),
                                                                                    (4,80, true, true, true);
