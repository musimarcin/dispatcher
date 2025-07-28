import React, { useEffect, useState } from "react";
import api from "../assets/api";
import "bootstrap/dist/css/bootstrap.min.css";

function Dashboard() {
  const [tab, setTab] = useState("vehicles"); // "vehicles" or "notifications"

  // Vehicles state
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [vehiclePage, setVehiclePage] = useState(0);
  const [vehicleTotalPages, setVehicleTotalPages] = useState(0);

  // Notifications state
  const [notifications, setNotifications] = useState([]);
  const [selectedNotification, setSelectedNotification] = useState(null);
  const [notificationPage, setNotificationPage] = useState(0);
  const [notificationTotalPages, setNotificationTotalPages] = useState(0);

  // Load vehicles
  useEffect(() => {
    if (tab !== "vehicles") return;

    api
      .get(`/vehicles?page=${vehiclePage + 1}`)
      .then((response) => {
        setVehicles(response.data.vehicleDtoList);
        setVehicleTotalPages(response.data.totalPages);
        setSelectedVehicle(null); // reset selection when page changes
      })
      .catch((err) => alert(err.response?.data || err.message));
  }, [tab, vehiclePage]);

  // Load notifications
  useEffect(() => {
    if (tab !== "notifications") return;

    api
      .get(`/notifications?page=${notificationPage + 1}`)
      .then((response) => {
        setNotifications(response.data.notificationDtoList);
        setNotificationTotalPages(response.data.totalPages);
        setSelectedNotification(null); // reset selection on page change
      })
      .catch((err) => alert(err.response?.data || err.message));
  }, [tab, notificationPage]);

  const selectNotification = (notification) => {
    api.post('/notifications/read', notification);
    setSelectedNotification(notification);
  }

  return (
    <div className="container mt-4">
      <h2 className="mb-3">Dashboard</h2>

      {/* Tabs */}
      <ul className="nav nav-tabs mb-3">
        <li className="nav-item">
          <button
            className={`nav-link ${tab === "vehicles" ? "active" : ""}`}
            onClick={() => setTab("vehicles")}
          >
            Vehicles
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${tab === "notifications" ? "active" : ""}`}
            onClick={() => setTab("notifications")}
          >
            Notifications
          </button>
        </li>
      </ul>

      {/* Vehicles Tab */}
      {tab === "vehicles" && (
        <>
          <ul className="list-group mb-4">
            {vehicles.length === 0 && <li>No vehicles found.</li>}
            {vehicles.map((vehicle) => (
              <li
                key={vehicle.id}
                className={`list-group-item ${
                  selectedVehicle?.id === vehicle.id ? "active" : ""
                }`}
                onClick={() => setSelectedVehicle(vehicle)}
                style={{ cursor: "pointer" }}
              >
                {vehicle.model} ({vehicle.licensePlate})
              </li>
            ))}
          </ul>

          <div className="d-flex justify-content-center align-items-center mb-3">
            <button
              className="btn btn-outline-primary me-2"
              disabled={vehiclePage === 0}
              onClick={() => setVehiclePage(vehiclePage - 1)}
            >
              Prev
            </button>
            <span>
              Page {vehiclePage + 1} of {vehicleTotalPages}
            </span>
            <button
              className="btn btn-outline-primary ms-2"
              disabled={vehiclePage + 1 === vehicleTotalPages}
              onClick={() => setVehiclePage(vehiclePage + 1)}
            >
              Next
            </button>
          </div>

          {selectedVehicle && (
            <div className="card">
              <div className="card-body">
                <h5 className="card-title">{selectedVehicle.model}</h5>
                <p className="card-text">
                  <strong>Manufacturer:</strong> {selectedVehicle.manufacturer}
                </p>
                <p className="card-text">
                  <strong>License Plate:</strong> {selectedVehicle.licensePlate}
                </p>
                <p className="card-text">
                  <strong>Fuel Capacity:</strong> {selectedVehicle.fuelCapacity} L
                </p>
                <p className="card-text">
                  <strong>Average Consumption:</strong>{" "}
                  {selectedVehicle.averageConsumption} L/100km
                </p>
                <p className="card-text">
                  <strong>Mileage:</strong> {selectedVehicle.mileage} km
                </p>
                <p className="card-text">
                  <strong>Last Maintenance:</strong>{" "}
                  {selectedVehicle.lastMaintenance || "N/A"}
                </p>
                <p className="card-text">
                  <strong>Created At:</strong> {selectedVehicle.createdAt || "N/A"}
                </p>
              </div>
            </div>
          )}
        </>
      )}

      {/* Notifications Tab */}
      {tab === "notifications" && (
        <>
          <ul className="list-group mb-4">
            {notifications.length === 0 && <li>No notifications found.</li>}
            {notifications.map((notification) => (
              <li
                key={notification.id}
                className={`list-group-item ${
                  selectedNotification?.id === notification.id ? "active" : ""
                } ${notification.isRead ? "" : "fw-bold"}`}
                onClick={() => selectNotification(notification)}
                style={{ cursor: "pointer" }}
              >
                {notification.message}
                <br />
                <small className="text-muted">
                  {new Date(notification.createdAt).toLocaleString()}
                </small>
              </li>
            ))}
          </ul>

          <div className="d-flex justify-content-center align-items-center mb-3">
            <button
              className="btn btn-outline-primary me-2"
              disabled={notificationPage === 0}
              onClick={() => setNotificationPage(notificationPage - 1)}
            >
              Prev
            </button>
            <span>
              Page {notificationPage + 1} of {notificationTotalPages}
            </span>
            <button
              className="btn btn-outline-primary ms-2"
              disabled={notificationPage + 1 === notificationTotalPages}
              onClick={() => setNotificationPage(notificationPage + 1)}
            >
              Next
            </button>
          </div>

          {selectedNotification && (
            <div className="card">
              <div className="card-body">
                <p>{selectedNotification.message}</p>
                <p>
                  <small className="text-muted">
                    Created:{" "}
                    {new Date(selectedNotification.createdAt).toLocaleString()}
                  </small>
                </p>
                <p>User ID: {selectedNotification.userId}</p>
                <p>Status: {selectedNotification.isRead ? "Read" : "Unread"}</p>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default Dashboard;
