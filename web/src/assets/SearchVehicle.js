import { useState, useEffect, useContext } from "react";
import api from "./api"
import { AuthContext } from "../assets/AuthContext";

function SearchVehicle({showToast}) {

    const { user } = useContext(AuthContext);

    const [search, setSearch] = useState({
        licensePlate: "",
        model: "",
        manufacturer: "",
        productionYearFrom: "",
        productionYearTo: "",
        fuelCapacityFrom: "",
        fuelCapacityTo: "",
        averageConsumptionFrom: "",
        averageConsumptionTo: "",
        mileageFrom: "",
        mileageTo: "",
    });

    const [vehicles, setVehicles] = useState([]);
    const [page, setPage] = useState(0);
    const [drivers, setDrivers] = useState([]);
    const [vehiclesTemp, setVehiclesTemp] = useState([]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setSearch((prev) => ({ ...prev, [name]: value }));
    };

//    useEffect(() => {
//        const updatedVehicles = vehicles.map(v => {
//            const driv = drivers.find(d => d.id === v.userId);
//            v.userId = `${v.userId}. ${driv.username}`;
//            return v;
//        });
//        setVehicles(updatedVehicles)
//    }, [drivers, vehicles])

    const handleSearch = async (e) => {
        e.preventDefault();
//        api.get('/user/drivers', search)
//        .then((response) => {
//            if (response.data.body == null) {
//                showToast(response.data.message, "error")
//                return;
//            }
//            setDrivers(response.data.body)
//        }).catch((err) => console.log(err))
//
//        let request = '';
//        if (user.roles.includes("DRIVER")) {
//            const un = user.username;
//            request = `&driver=${un}`;
//        }
//        const url = `/vehicle/search?page=${page}${request}`;

        api.post(`/vehicle/search?page=${page}`, search)
        .then((response) => {
            if (response.data.body == null) {
                showToast(response.data.message, "error")
                return;
            }
            setVehicles(response.data.body.vehicleDtoList);
        }).catch((err) => console.log(err))
    };

    const removeVehicle = (licensePlate) => {
        api.delete(`/vehicle?licensePlate=${licensePlate}`)
        .then(res => showToast(res.data.message, "success"))
        .catch(err => showToast(err.response?.data.message, "error"))
    }

    return (
        <div className="container my-5">
            <h2>Search Vehicles</h2>
            <form onSubmit={handleSearch} className="mb-4">
                <div className="row">
                    <div className="col-md-4 mb-2">
                        <input
                            type="text"
                            className="form-control"
                            name="licensePlate"
                            placeholder="License Plate"
                            value={search.licensePlate}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-4 mb-2">
                        <input
                            type="text"
                            className="form-control"
                            name="model"
                            placeholder="Model"
                            value={search.model}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-4 mb-2">
                        <input
                            type="text"
                            className="form-control"
                            name="manufacturer"
                            placeholder="Manufacturer"
                            value={search.manufacturer}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-3 mb-2">
                        <input
                            type="number"
                            className="form-control"
                            name="productionYearFrom"
                            placeholder="Production Year From"
                            min="1900"
                            max="2100"
                            value={search.productionYearFrom}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-3 mb-2">
                        <input
                            type="number"
                            className="form-control"
                            name="productionYearTo"
                            placeholder="Production Year To"
                            min="1900"
                            max="2100"
                            value={search.productionYearTo}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-3 mb-2">
                        <input
                            type="number"
                            className="form-control"
                            name="fuelCapacityFrom"
                            placeholder="Fuel Capacity From"
                            value={search.fuelCapacityFrom}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-3 mb-2">
                        <input
                            type="number"
                            className="form-control"
                            name="fuelCapacityTo"
                            placeholder="Fuel Capacity To"
                            value={search.fuelCapacityTo}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-3 mb-2">
                        <input
                            type="number"
                            className="form-control"
                            name="averageConsumptionFrom"
                            placeholder="Avg. Consumption From"
                            value={search.averageConsumptionFrom}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-3 mb-2">
                        <input
                            type="number"
                            className="form-control"
                            name="averageConsumptionTo"
                            placeholder="Avg. Consumption To"
                            value={search.averageConsumptionTo}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-3 mb-2">
                        <input
                            type="number"
                            className="form-control"
                            name="mileageFrom"
                            placeholder="Mileage From"
                            value={search.mileageFrom}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="col-md-3 mb-2">
                        <input
                            type="number"
                            className="form-control"
                            name="mileageTo"
                            placeholder="Mileage To"
                            value={search.mileageTo}
                            onChange={handleChange}
                        />
                    </div>
                </div>

                <button type="submit" className="btn btn-primary">
                    Search
                </button>
            </form>

            {vehicles?.length > 0 && (
                <div>
                    <h4>Results:</h4>
                    <table className="table table-bordered">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>License Plate</th>
                                <th>Model</th>
                                <th>Manufacturer</th>
                                <th>Fuel Capacity</th>
                                <th>Avg. Consumption</th>
                                <th>Mileage</th>
                                <th>Driver ID</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {vehicles.map((v) => (
                                <tr key={v.id}>
                                    <td>{v.id}</td>
                                    <td>{v.licensePlate}</td>
                                    <td>{v.model}</td>
                                    <td>{v.manufacturer}</td>
                                    <td>{v.fuelCapacity}</td>
                                    <td>{v.averageConsumption}</td>
                                    <td>{v.mileage}</td>
                                    <td>{v.userId}</td>
                                    <td>
                                        <button className="btn btn-danger mt-2" onClick={() => removeVehicle(v.licensePlate)}>
                                            Delete Vehicle
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
export default SearchVehicle;