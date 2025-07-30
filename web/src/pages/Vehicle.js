import { useState } from "react";
import api from "../assets/api";
import { useNavigate } from "react-router-dom";
import SearchVehicle from "../assets/SearchVehicle"
import 'bootstrap/dist/css/bootstrap.min.css';

function Vehicle() {
    const [vehicle, setVehicle] = useState({
        licensePlate: "",
        model: "",
        manufacturer: "",
        productionYear: "",
        fuelCapacity: "",
        averageConsumption: "",
        mileage: "",
        lastMaintenance: "",
    });

    const navigate = useNavigate();

    const handleChange = (e) => {
        setVehicle({
            ...vehicle,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        await api.post("/vehicles", {
            ...vehicle,
            fuelCapacity: parseFloat(vehicle.fuelCapacity),
            averageConsumption: vehicle.averageConsumption
            ? parseFloat(vehicle.averageConsumption)
            : null,
            mileage: parseInt(vehicle.mileage),
            lastMaintenance: vehicle.lastMaintenance || null,
        })
        .then(res => {
            alert(res.data);
            navigate("/");
        }).catch(err => alert(err.response?.data));
    };

    return (
        <>
        <div className="m-3">
            <h2 className="mb-4 mx-5">Add New Vehicle</h2>
            <form onSubmit={handleSubmit} className="d-flex justify-content-center align-items-center">
                <input
                    type="text"
                    name="licensePlate"
                    placeholder="License Plate"
                    value={vehicle.licensePlate}
                    onChange={handleChange}
                    required
                    className="p-2 border rounded mb-1"
                />

                <input
                    type="text"
                    name="model"
                    placeholder="Model"
                    value={vehicle.model}
                    onChange={handleChange}
                    required
                    className="p-2 border rounded mx-1 mb-1"
                />

                <input
                    type="number"
                    name="productionYear"
                    placeholder="Production Year"
                    value={vehicle.productionYear}
                    onChange={handleChange}
                    min="1900"
                    required
                    className="p-2 border rounded mx-1 mb-1"
                />

                <input
                    type="text"
                    name="manufacturer"
                    placeholder="Manufacturer"
                    value={vehicle.manufacturer}
                    onChange={handleChange}
                    required
                    className="p-2 border rounded mx-1 mb-1"
                />

                <input
                    type="number"
                    name="fuelCapacity"
                    placeholder="Fuel Capacity"
                    value={vehicle.fuelCapacity}
                    onChange={handleChange}
                    required
                    min="1"
                    step="0.1"
                    className="p-2 border rounded mx-1 mb-1"
                />

                <input
                    type="number"
                    name="averageConsumption"
                    placeholder="Average Consumption (optional)"
                    value={vehicle.averageConsumption}
                    onChange={handleChange}
                    min="1"
                    step="0.1"
                    className="p-2 border rounded mx-1 mb-1"
                />

                <input
                    type="number"
                    name="mileage"
                    placeholder="Mileage"
                    value={vehicle.mileage}
                    onChange={handleChange}
                    required
                    min="0"
                    className="p-2 border rounded mx-1 mb-1"
                />

                <input
                    type="date"
                    name="lastMaintenance"
                    value={vehicle.lastMaintenance}
                    onChange={handleChange}
                    className="p-2 border rounded mx-1 mb-1"
                />

                <button
                    type="submit"
                    className="btn btn-primary mb-1"
                >
                    Add Vehicle
                </button>
            </form>
        </div>

        <div className="border rounded shadow m-5">
            <SearchVehicle />
        </div>
        </>
    );
}
export default Vehicle;