package com.skytycoon.app.domain.model

enum class GameMode { REALISTIC, FICTIONAL }

enum class OperationType { AIRLINE, CHARTER, HELICOPTER }

enum class AircraftCategory { AIRLINER, CHARTER, HELICOPTER }

enum class FlightStatus { SCHEDULED, BOARDING, IN_FLIGHT, COMPLETED, CANCELLED, DELAYED }

enum class EmployeeType { PILOT, HELICOPTER_PILOT, COPILOT, FLIGHT_ATTENDANT, MECHANIC, ADMIN }

enum class MissionType { PRIMARY, SECONDARY, DAILY }

enum class MissionStatus { ACTIVE, COMPLETED, EXPIRED }

enum class AcquisitionType { PURCHASED, LEASED }

enum class ContractStatus { AVAILABLE, ACCEPTED, COMPLETED, FAILED, EXPIRED }
