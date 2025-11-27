import api from '../assets/api'
import { useState, useEffect, useRef } from 'react';

function FuelHistories({vehId, showToast}) {

    const [fuelHistory, setFuelHistory] = useState([])

    useEffect(() => {
        api.get(`/fuel/history?vehicleId=${vehId}`)
        .then(response => {
            if (response.data.body == null) {
                showToast(response.data.message, "error")
                return;
            }
            setFuelHistory(response.data.body.fuelHistoryDtoList)
        }).catch(err => console.log(err))
    }, [vehId])

    return (
        <>
            <div className="container mt-4">
                <h2>Fuel History</h2>
                    <table className="table table-striped">
                        <thead>
                            <tr>
                                <th>Fuel consumed</th>
                                <th>Creation date</th>
                                <th>Vehicle Id</th>
                                <th>Route Id</th>
                            </tr>
                        </thead>
                        <tbody>
                            {fuelHistory.map((f) => (
                                <tr key={f.id}>
                                    <td>{f.fuelConsumed}</td>
                                    <td>{new Date(f.createdAt).toLocaleString("pl-Pl")}</td>
                                    <td>{f.vehicleId}</td>
                                    <td>{f.routeId}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
            </div>
        </>
    )
}
export default FuelHistories;