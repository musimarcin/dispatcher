import { useState, useContext } from "react";
import api from "../assets/api";
import { useNavigate } from "react-router-dom";
import SearchVehicle from "../assets/SearchVehicle"
import 'bootstrap/dist/css/bootstrap.min.css';
import { AuthContext } from "../assets/AuthContext";

function Vehicle({showToast}) {

    const { user } = useContext(AuthContext);

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

        await api.post("/vehicle", {
            ...vehicle,
            fuelCapacity: parseFloat(vehicle.fuelCapacity),
            averageConsumption: vehicle.averageConsumption
            ? parseFloat(vehicle.averageConsumption)
            : null,
            mileage: parseInt(vehicle.mileage),
            lastMaintenance: vehicle.lastMaintenance || null,
        })
        .then(res => {
            showToast(res.data.message, "success");
            navigate("/");
        }).catch(err => showToast(err.response?.data.message, "error"));
    };

    return (
        <>
        {user.roles.includes("DRIVER") && (
        <div className="m-3">
            <h2 className="m-4">Add New Vehicle</h2>
            <div className="d-flex justify-content-between ms-4">
                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        name="licensePlate"
                        placeholder="License Plate"
                        value={vehicle.licensePlate}
                        onChange={handleChange}
                        required
                        className="p-2 border rounded mb-1 me-1"
                    />

                    <input
                        type="text"
                        name="model"
                        placeholder="Model"
                        value={vehicle.model}
                        onChange={handleChange}
                        required
                        className="p-2 border rounded me-1 mb-1"
                    />

                    <input
                        type="number"
                        name="productionYear"
                        placeholder="Production Year"
                        value={vehicle.productionYear}
                        onChange={handleChange}
                        min="1900"
                        required
                        className="p-2 border rounded me-1 mb-1"
                    />

                    <input
                        type="text"
                        name="manufacturer"
                        placeholder="Manufacturer"
                        value={vehicle.manufacturer}
                        onChange={handleChange}
                        required
                        className="p-2 border rounded me-1 mb-1"
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
                        className="p-2 border rounded me-1 mb-1"
                    />

                    <input
                        type="number"
                        name="averageConsumption"
                        placeholder="Average Consumption (optional)"
                        value={vehicle.averageConsumption}
                        onChange={handleChange}
                        min="1"
                        step="0.1"
                        className="p-2 border rounded me-1 mb-1"
                    />

                    <input
                        type="number"
                        name="mileage"
                        placeholder="Mileage"
                        value={vehicle.mileage}
                        onChange={handleChange}
                        required
                        min="0"
                        className="p-2 border rounded me-1 mb-1"
                    />

                    <input
                        type="date"
                        name="lastMaintenance"
                        value={vehicle.lastMaintenance}
                        onChange={handleChange}
                        className="p-2 border rounded me-1 mb-1"
                    />

                    <button
                        type="submit"
                        className="btn btn-primary mb-1"
                    >
                        Add Vehicle
                    </button>
                </form>
            </div>
        </div>
        )}

        <div className="border rounded shadow m-5">
            <SearchVehicle showToast={showToast} />
        </div>
        </>
    );
}
export default Vehicle;