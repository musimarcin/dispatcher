import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import "leaflet-routing-machine";
import 'bootstrap/dist/css/bootstrap.min.css';
import api from '../assets/api'
import { useState, useEffect, useRef } from 'react';
import markerImg from '../assets/marker-icon.png';
import shadowImg from '../assets/marker-shadow.png';

function Route({showToast}) {

    const mapRef = useRef(null);
    const routingControlRef = useRef(null);
    const [isPopUp, setIsPopUp] = useState(false);

    const [locations, setLocations] = useState([
        { id: 1, query: "", street: "", city: "", county: "", state: "", country: "", postalCode: "", coords: null },
        { id: 2, query: "", street: "", city: "", county: "", state: "", country: "", postalCode: "", coords: null },
    ]);

    const [suggestions, setSuggestions] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [selectedVehicle, setSelectedVehicle] = useState(null);
    const [routes, setRoutes] = useState([]);
    const [distance, setDistance] = useState(0);
    const [estimatedTime, setEstimatedTime] = useState(0);
    const [tankBefore, setTankBefore] = useState(0);
    const [tankAfter, setTankAfter] = useState(0);
    const [fuelUsed, setFuelUsed] = useState(0);
    const [routeId, setRouteId] = useState(0);

    useEffect(() => {

        const map = L.map('map').setView([52.2297, 21.0122], 12);
        mapRef.current = map;

        L.tileLayer('https://tiles.stadiamaps.com/tiles/alidade_satellite/{z}/{x}/{y}{r}.{ext}', {
        	minZoom: 0,
        	maxZoom: 200,
        	attribution: '&copy; CNES, Distribution Airbus DS, © Airbus DS, © PlanetObserver (Contains Copernicus Data) | &copy; <a href="https://www.stadiamaps.com/" target="_blank">Stadia Maps</a> &copy; <a href="https://openmaptiles.org/" target="_blank">OpenMapTiles</a> &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        	ext: 'jpg'
        }).addTo(map);

        mapRef.current = map;

        api.get("/vehicle")
        .then(response => {
            if (response.data.body == null) {
                showToast(response.data.message, "error")
                return;
            }
            setVehicles(response.data.body.vehicleDtoList)
        }).catch((err) => console.log(err))

        return () => {
            if (routingControlRef.current) routingControlRef.current.remove();
            map.remove();
        };

    }, []);

    useEffect(() => {
        const waypoints = getWaypoints();
        if (waypoints?.length > 1) updateRoute(waypoints);
    }, [locations]);

    const customIcon = new L.Icon({
        iconUrl: markerImg,
        shadowUrl: shadowImg,
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41],
    });

    const addLocation = () => {
        const nextId = locations.length + 1;
        setLocations((prev) => [
            ...prev,
            { id: nextId, query: "", street: "", city: "", county: "", state: "", country: "", postalCode: "", coords: null },
        ]);
    };

    const removeLocation = (id) => {
        setLocations((prevLocations) =>
            prevLocations.filter((loc) => loc.id !== id)
        );
    }

    const handleQueryChange = async (id, value) => {
        setLocations((prev) =>
            prev.map((loc) => (loc.id === id ? { ...loc, query:value } : loc))
        )

        if (value.length < 3) {
            setSuggestions([]);
            return;
        }

        api.get(`/nominatim?q=${value}`)
        .then(response => {
            if (response.data.body == null) {
                showToast(response.data.message, "error")
                return;
            }
            setSuggestions(response.data.body.map((s) => ({ ...s, locId: id})))
        }).catch((err) => console.log(err))
    };

    const handleSuggestionClick = (s) => {
        const { lat, lon, display_name, locId } = s;

        setLocations((prev) =>
            prev.map((loc) =>
                loc.id === locId
                ? { ...loc, query: display_name, coords: { lat: parseFloat(lat), lon: parseFloat(lon)}}
                : loc
            )
        )

        setSuggestions([]);
        const waypoints = getWaypoints();
        updateRoute(waypoints);
    };

    const handleStructuredSearch = async (id) => {
        const loc = locations.find((l) => l.id === id);
        if (!loc) return;

        api.post("/nominatim", {
            street: loc.street,
            city: loc.city,
            county: loc.county,
            state: loc.state,
            country: loc.country,
            postalCode: loc.postalCode
        }).then(response => {
            const results = response.data.body
            if (results == null) {
                showToast(response.data.message, "error")
                return;
            }
            if (results.length > 0) {
                const { lat, lon, display_name } = results[0];
                setLocations((prev) =>
                    prev.map((loc) =>
                        loc.id === id
                            ? { ...loc, query: display_name, coords: { lat: parseFloat(lat), lon: parseFloat(lon) } }
                            : loc
                    )
                )
                const waypoints = getWaypoints();
                updateRoute(waypoints);
            }
        }).catch((err) => console.log(err));
    };

    const getWaypoints = () => {
        const coordsArray = locations.filter((l) => l.coords).map((l) => l.coords);
        if (coordsArray.length === 0 || !mapRef.current) return;

        if (coordsArray.length === 1) {
            mapRef.current.setView([coordsArray[0].lat, coordsArray[0].lon], 13);
            return;
        }
        const waypoints = coordsArray.map((c) => L.latLng(c.lat, c.lon));
        return waypoints;
    }


    const updateRoute = (waypoints) => {
        if (routingControlRef.current) {
            routingControlRef.current.setWaypoints(waypoints);
        } else {
            routingControlRef.current = L.Routing.control({
                waypoints,
                router: L.Routing.osrmv1({ serviceUrl: "http://localhost:8080/osrm/route/v1" }),
                createMarker: (i, wp) => L.marker(wp.latLng, { icon: customIcon, draggable: true }),
            }).addTo(mapRef.current);
            routingControlRef.current.on('routesfound', function (e) {
                const routes = e.routes;
                const routeDistance = (routes[0].summary.totalDistance / 1000).toFixed(2);
                const routeDuration = routes[0].summary.totalTime;
                setDistance(routeDistance);
                setEstimatedTime(routeDuration);
            })
        }
        const group = new L.featureGroup(waypoints.map((c) => L.marker(c)));
        mapRef.current.fitBounds(group.getBounds(), { padding: [50, 50] });
    }

    const displayRouteOnMap = (id) => {
        if (!mapRef.current) return;

        const route = routes.find(r => r.id === id);
        if (!route || !route.waypoints) return;

        const waypoints = route.waypoints.map(wp => L.latLng(wp.latitude, wp.longitude));

        updateRoute(waypoints);
    };

    const handleVehicleChange = (e) => {
        const vehiclePlate = e.target.value;
        if (!vehiclePlate) {
            setSelectedVehicle(null)
            setRoutes([]);
            return;
        }
        const vehicle = vehicles.find(v => v.licensePlate === vehiclePlate);
        setSelectedVehicle(vehicle);

        api.get(`/route/vehicle?licensePlate=${vehiclePlate}`)
            .then(response => {
                if (response.data.body == null) {
                    showToast(response.data.message, "error")
                    return;
                }
                setRoutes(response.data.body.routeDtoList)
            }).catch(err => console.log(err))
    };

    const getCoordsString = (id) => {
        const loc = locations.find(location => location.id === id);
        return loc?.coords ? `${loc.coords.lat},${loc.coords.lon}` : null;
    };

    const addLocationToVehicle = () => {
        if (!selectedVehicle) {
            showToast("Please make sure to select a vehicle.", "error");
            return;
        }
        const startTime = new Date();
        const endTime = new Date(startTime.getTime() + estimatedTime * 1000);
        const newRoute = {
            distance,
            estimatedTime,
            startTime,
            endTime,
            createdAt: new Date(),
            vehicleDto: selectedVehicle,
            waypoints: locations
                .filter(loc => loc.coords)
                .map((loc, i) => ({
                    name: loc.query,
                    latitude: loc.coords.lat,
                    longitude: loc.coords.lon,
                    sequence: i + 1,
                }))
        };

        api.post("/route",
            newRoute
        ).then(res => {
            showToast(res.data.message, "success");
            handleVehicleChange({ target: { value: selectedVehicle.licensePlate } });
        })
        .catch(err => showToast(err.response?.data.message, "error"))
    }

    const removeRoute = (id) => {
        api.delete(`/route?id=${id}`)
        .then(res => {
            showToast(res.data.message, "success")
            setRoutes(prevRoutes => prevRoutes.filter(r => r.id !== id));
        }).catch(err => showToast(err.response?.data.message, "error"))
    }

    const calculateFuel = () => {
        if (tankAfter > tankBefore) {
            showToast("Tank after cannot exceed tank before", "error")
            return
        }
        const fuel = tankBefore - tankAfter;
        api.put("/vehicle/route", {
            id : selectedVehicle.id,
            averageConsumption : fuel,
            mileage : distance
        }).then(res => showToast(res.data.message, "success")
        ).catch(err => showToast(err.response?.data.message, "error"))

        api.put("/route", {
            id : routeId,
            status: "FINISHED"
        }).then(res => {
            showToast(res.data.message, "success")
            handleVehicleChange({ target: { value: selectedVehicle.licensePlate } });
        }).catch(err => showToast(err.response?.data.message, "error"))
        setIsPopUp(false);
    }

    const getDirectFuel = () => {
        api.put("/vehicle/route", {
            id : selectedVehicle.id,
            averageConsumption : fuelUsed,
            mileage : distance
        }).then(res => showToast(res.data.message, "success")
        ).catch(err => showToast(err.response?.data.message, "error"))

        api.put("/route", {
            id : routeId,
            status: "FINISHED"
        }).then(res => {
            showToast(res.data.message, "success")
            handleVehicleChange({ target: { value: selectedVehicle.licensePlate } });
        }).catch(err => showToast(err.response?.data.message, "error"))
        setIsPopUp(false);
    }

    const startRoute = (id) => {
        api.put("/route", {
            id,
            status: "ACTIVE"
        }).then(res => {
            showToast(res.data.message, "success")
            handleVehicleChange({ target: { value: selectedVehicle.licensePlate } });
        }).catch(err => showToast(err.response?.data.message, "error"))
    }


    return (
        <>
            <div className="container mt-4">
                <h2>Routes</h2>

                <div className="mb-3">
                    <label htmlFor="vehicle" className="form-label">Select Vehicle</label>
                    <select
                        id="vehicle"
                        className="form-select"
                        value={selectedVehicle?.licensePlate || ""}
                        onChange={handleVehicleChange}
                    >
                        <option value="">-- Choose Vehicle --</option>
                            {vehicles.map((v) => (
                                <option key={v.licensePlate} value={v.licensePlate}>
                                    {v.model} ({v.licensePlate})
                                </option>
                            ))}
                    </select>
                </div>

                {routes.length > 0 ? (
                    <table className="table table-striped">
                        <thead>
                            <tr>
                                <th>Route ID</th>
                                <th>Start</th>
                                <th>Through</th>
                                <th>Finish</th>
                                <th>Distance (km)</th>
                                <th>Estimated Time (min)</th>
                                <th>Date</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {routes.map((r) => {
                                const firstWaypoint = r.waypoints?.[0]?.name || "";
                                const through = r.waypoints?.slice(1, -1).map(wp => wp.name).join(", ") || "";
                                const lastWaypoint = r.waypoints?.[r.waypoints.length - 1]?.name || "";

                                return (
                                    <tr key={r.id}>
                                        <td>{r.id}</td>
                                        <td>{firstWaypoint}</td>
                                        <td>{through}</td>
                                        <td>{lastWaypoint}</td>
                                        <td>{r.distance}</td>
                                        <td>{(r.estimatedTime / 60).toFixed(2)}</td>
                                        <td>{new Date(r.createdAt).toLocaleString("pl-PL")}</td>
                                        <td>{r.status}</td>
                                        <td>
                                            <button className="btn btn-danger mt-2" onClick={() => removeRoute(r.id)}>
                                                Remove Route
                                            </button>
                                            <button className="btn btn-primary mt-2" onClick={() => displayRouteOnMap(r.id)}>
                                                Show on map
                                            </button>
                                            {r.status === "PLANNED" && (
                                                <button className="btn btn-primary mt-2" onClick={() => startRoute(r.id)}>
                                                    Start route
                                                </button>
                                            )}
                                            {r.status === "ACTIVE" && (
                                                <button className="btn btn-success mt-2" onClick={() => {
                                                        setIsPopUp(true)
                                                        setDistance(r.distance)
                                                        setRouteId(r.id)
                                                    }
                                                }>
                                                    Set as completed
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                )
                            })}
                        </tbody>
                    </table>
                ) : (
                    selectedVehicle && <p>No routes available for this vehicle.</p>
                )}
            </div>
            {locations.map((loc) => (
                <div key={loc.id} className="container mb-3 border p-2 rounded">
                    <input
                        type="text"
                        placeholder="Search..."
                        value={loc.query}
                        onChange={(e) => handleQueryChange(loc.id, e.target.value)}
                        className="form-control mb-1"
                    />
                    {suggestions
                        .filter((s) => s.locId === loc.id)
                        .map((s, i) => (
                        <div
                            key={i}
                            className="list-group-item list-group-item-action"
                            onClick={() => handleSuggestionClick(s)}
                            style={{ cursor: "pointer" }}
                        >
                        {s.display_name}
                        </div>
                    ))}
                    <div className="d-flex gap-2 flex-wrap">
                            <input type="text" placeholder="Street" value={loc.street}
                                onChange={(e) => setLocations((prev) => prev.map(l => l.id===loc.id ? {...l, street: e.target.value} : l))}
                                className="mr-2 p-2 border rounded" />
                            <input type="text" placeholder="City" value={loc.city}
                                onChange={(e) => setLocations((prev) => prev.map(l => l.id===loc.id ? {...l, city: e.target.value} : l))}
                                className="mr-2 p-2 border rounded" />
                            <input type="text" placeholder="County" value={loc.county}
                                onChange={(e) => setLocations((prev) => prev.map(l => l.id===loc.id ? {...l, county: e.target.value} : l))}
                                className="mr-2 p-2 border rounded" />
                            <input type="text" placeholder="State" value={loc.state}
                                onChange={(e) => setLocations((prev) => prev.map(l => l.id===loc.id ? {...l, state: e.target.value} : l))}
                                className="mr-2 p-2 border rounded" />
                            <input type="text" placeholder="Country" value={loc.country}
                                onChange={(e) => setLocations((prev) => prev.map(l => l.id===loc.id ? {...l, country: e.target.value} : l))}
                                className="mr-2 p-2 border rounded" />
                            <input type="text" placeholder="Postal Code" value={loc.postalCode}
                                onChange={(e) => setLocations((prev) => prev.map(l => l.id===loc.id ? {...l, postalCode: e.target.value} : l))}
                                className="mr-2 p-2 border rounded" />
                    </div>
                        <button className="btn btn-primary mt-2 me-1" onClick={() => handleStructuredSearch(loc.id)}>
                            Structured Search
                        </button>
                        {locations.length > 2 && (
                            <button
                                className="btn btn-danger mt-2"
                                onClick={() => removeLocation(loc.id)}
                            >
                                Remove Location
                            </button>
                        )}
                </div>
            ))}
            <div className="container">
                <button className="btn btn-success m-1" onClick={addLocation}>
                    Add Location
                </button>

                <button className="btn btn-success ms-1" onClick={addLocationToVehicle}>
                    Add Location to Vehicle
                </button>
            </div>
            <div id="map" className="d-flex justify-content-center"></div>
            {isPopUp && (
                <div className="modal show d-block" tabIndex="-1">
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">Fuel consumption</h5>
                                <button
                                    type="button"
                                    className="btn-close"
                                    onClick={() => setIsPopUp(false)}
                                ></button>
                            </div>
                            <div className="modal-body">
                                <p>Enter how much fuel you used or tank before and after</p>
                            </div>
                            <div className="modal-footer">
                                <input
                                    type="text"
                                    placeholder="Fuel tank before"
                                    value={tankBefore}
                                    onChange={(e) => setTankBefore(e.target.value)}
                                    className="form-control mb-1"
                                />
                                <input
                                    type="text"
                                    placeholder="Fuel tank after"
                                    value={tankAfter}
                                    onChange={(e) => setTankAfter(e.target.value)}
                                    className="form-control mb-1"
                                />
                                <button
                                    className="btn btn-primary"
                                    onClick={() => calculateFuel()}
                                >
                                    Calculate fuel consumption
                                </button>
                                <input
                                    type="text"
                                    placeholder="Fuel used"
                                    value={fuelUsed}
                                    onChange={(e) => setFuelUsed(e.target.value)}
                                    className="form-control mb-1"
                                />
                                <button
                                    className="btn btn-primary"
                                    onClick={() => getDirectFuel()}
                                >
                                    Submit fuel consumption
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}

export default Route;