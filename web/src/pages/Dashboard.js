import React, { useEffect, useState } from 'react';
import api from '../assets/api';
import 'bootstrap/dist/css/bootstrap.min.css';

function Dashboard() {
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    api.get(`/vehicles?page=${page+1}`)
      .then(response => {
        setVehicles(response.data.vehicleDtoList);
        setTotalPages(response.data.totalPages);
      })
      .catch(err => alert(err.response?.data));
  }, [page]);

  return (
    <div className="container mt-4">
      <h2 className="mb-3">Your Vehicles</h2>

      <ul className="list-group mb-4">
        {vehicles.map(vehicle => (
          <li
            key={vehicle.id}
            className={`list-group-item ${selectedVehicle?.id === vehicle.id ? 'active' : ''}`}
            onClick={() => setSelectedVehicle(vehicle)}
            style={{ cursor: 'pointer' }}
          >
            {vehicle.model} ({vehicle.licensePlate})
          </li>
        ))}
      </ul>

      {/* Pagination controls */}
      <div className="d-flex justify-content-center align-items-center mb-3">
        <button
          className="btn btn-outline-primary me-2"
          disabled={page === 0}
          onClick={() => setPage(page - 1)}
        >
          Prev
        </button>
        <span>Page {page + 1} of {totalPages}</span>
        <button
          className="btn btn-outline-primary ms-2"
          disabled={page + 1 === totalPages}
          onClick={() => setPage(page + 1)}
        >
          Next
        </button>
      </div>

      {/* Vehicle details */}
      {selectedVehicle && (
        <div className="card">
          <div className="card-body">
            <h5 className="card-title">{selectedVehicle.model}</h5>
            <p className="card-text"><strong>Manufacturer:</strong> {selectedVehicle.manufacturer}</p>
            <p className="card-text"><strong>License Plate:</strong> {selectedVehicle.licensePlate}</p>
            <p className="card-text"><strong>Fuel Capacity:</strong> {selectedVehicle.fuelCapacity} L</p>
            <p className="card-text"><strong>Average Consumption:</strong> {selectedVehicle.averageConsumption} L/100km</p>
            <p className="card-text"><strong>Mileage:</strong> {selectedVehicle.mileage} km</p>
            <p className="card-text"><strong>Last Maintenance:</strong> {selectedVehicle.lastMaintenance || "N/A"}</p>
            <p className="card-text"><strong>Created At:</strong> {selectedVehicle.createdAt || "N/A"}</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;