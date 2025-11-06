import { useEffect, useState } from "react";
import api from "../assets/api";

function Vehicles({showToast}) {

    const [vehicles, setVehicles] = useState([]);
    const [selectedVehicle, setSelectedVehicle] = useState(null);
    const [vehiclePage, setVehiclePage] = useState(0);
    const [vehicleTotalPages, setVehicleTotalPages] = useState(0);

    useEffect(() => {
        api
        .get(`/vehicles?page=${vehiclePage + 1}`)
        .then((response) => {
            setVehicles(response.data.vehicleDtoList);
            setVehicleTotalPages(response.data.totalPages);
            setSelectedVehicle(null); // reset selection when page changes
        })
        .catch((err) => showToast(err.response?.data || err.message, "error"));
    }, [vehiclePage]);

    return (
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
                        {new Date(selectedVehicle.lastMaintenance).toLocaleDateString("pl-PL") || "N/A"}
                    </p>

                    <p className="card-text">
                        <strong>Created At:</strong> {new Date(selectedVehicle.createdAt).toLocaleString("pl-PL") || "N/A"}
                    </p>

                </div>
            </div>
        )}
        </>
    )
}
export default Vehicles;