CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

CREATE TABLE Appointments (
    AppointmentID int, 
    Time Date,
    Username_P varchar(255) REFERENCES Patients,
    Username varchar(255) REFERENCES Caregivers,
    Name varchar(255) REFERENCES Vaccines,
    PRIMARY KEY(AppointmentID); 
);

CREATE TABLE Patients (
	Username_P varchar(255),
	Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);