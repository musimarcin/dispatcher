import React, { useState } from "react";
import api from "../assets/api";
import { useNavigate } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';

function Vehicle() {
  const [vehicle, setVehicle] = useState({
    licensePlate: "",
    model: "",
    manufacturer: "",
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

    try {
      await api.post("/vehicles", {
        ...vehicle,
        fuelCapacity: parseFloat(vehicle.fuelCapacity),
        averageConsumption: vehicle.averageConsumption
          ? parseFloat(vehicle.averageConsumption)
          : null,
        mileage: parseInt(vehicle.mileage),
        lastMaintenance: vehicle.lastMaintenance || null,
      });

      alert("Vehicle added successfully!");
      navigate("/");
    } catch (err) {
      alert(err.response?.data || "Error adding vehicle");
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10 p-4 border rounded shadow">
      <h2 className="text-2xl font-semibold mb-4">Add New Vehicle</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="text"
          name="licensePlate"
          placeholder="License Plate"
          value={vehicle.licensePlate}
          onChange={handleChange}
          required
          className="w-full p-2 border rounded"
        />
        <input
          type="text"
          name="model"
          placeholder="Model"
          value={vehicle.model}
          onChange={handleChange}
          required
          className="w-full p-2 border rounded"
        />
        <input
          type="text"
          name="manufacturer"
          placeholder="Manufacturer"
          value={vehicle.manufacturer}
          onChange={handleChange}
          required
          className="w-full p-2 border rounded"
        />
        <input
          type="number"
          name="fuelCapacity"
          placeholder="Fuel Capacity"
          value={vehicle.fuelCapacity}
          onChange={handleChange}
          required
          step="0.01"
          className="w-full p-2 border rounded"
        />
        <input
          type="number"
          name="averageConsumption"
          placeholder="Average Consumption (optional)"
          value={vehicle.averageConsumption}
          onChange={handleChange}
          step="0.01"
          className="w-full p-2 border rounded"
        />
        <input
          type="number"
          name="mileage"
          placeholder="Mileage"
          value={vehicle.mileage}
          onChange={handleChange}
          required
          className="w-full p-2 border rounded"
        />
        <input
          type="date"
          name="lastMaintenance"
          value={vehicle.lastMaintenance}
          onChange={handleChange}
          className="w-full p-2 border rounded"
        />

        <button
          type="submit"
          className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600"
        >
          Add Vehicle
        </button>
      </form>
    </div>
  );
}
export default Vehicle;