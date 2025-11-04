import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import "leaflet-routing-machine";
import 'bootstrap/dist/css/bootstrap.min.css';
import api from '../assets/api'
import { useState, useEffect, useRef } from 'react';
import markerImg from '../assets/marker-icon.png';
import shadowImg from '../assets/marker-shadow.png';

function Route() {

    const mapRef = useRef(null);
    const routingControlRef = useRef(null);

    const [locations, setLocations] = useState([
        { id: 1, query: "", street: "", city: "", county: "", state: "", country: "", postalCode: "", coords: null },
        { id: 2, query: "", street: "", city: "", county: "", state: "", country: "", postalCode: "", coords: null },
    ]);

    const [suggestions, setSuggestions] = useState([]);

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

        return () => {
            if (routingControlRef.current) routingControlRef.current.remove();
            map.remove();
        };

    }, []);

    useEffect(() => {
        updateRoute();
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
        setLocations((prev) => [
            ...prev,
            { id: Date.now(), query: "", street: "", city: "", county: "", state: "", country: "", postalCode: "", coords: null },
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
            setSuggestions(response.data.map((s) => ({ ...s, locId: id})))
        }).catch(err => alert(err.response?.data))
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
        updateRoute();
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
            const results = response.data
            if (results.length > 0) {
                console.log(results);
                const { lat, lon, display_name } = results[0];
                setLocations((prev) =>
                    prev.map((loc) =>
                        loc.id === id
                            ? { ...loc, query: display_name, coords: { lat: parseFloat(lat), lon: parseFloat(lon) } }
                            : loc
                    )
                )
                updateRoute();
            }
        }).catch(err => alert(err.response?.data));
    };


    const updateRoute = () => {
        const coordsArray = locations.filter((l) => l.coords).map((l) => l.coords);
        if (coordsArray.length === 0 || !mapRef.current) return;

        if (coordsArray.length === 1) {
            mapRef.current.setView([coordsArray[0].lat, coordsArray[0].lon], 13);
            return;
        }

        const waypoints = coordsArray.map((c) => L.latLng(c.lat, c.lon));

        if (routingControlRef.current) {
            routingControlRef.current.setWaypoints(waypoints);
        } else {
            routingControlRef.current = L.Routing.control({
                waypoints,
                router: L.Routing.osrmv1({ serviceUrl: "http://localhost:8080/osrm/route/v1" }),
                createMarker: (i, wp) => L.marker(wp.latLng, { icon: customIcon, draggable: true }),
            }).addTo(mapRef.current);
        }
        const group = new L.featureGroup(waypoints.map((c) => L.marker(c)));
        mapRef.current.fitBounds(group.getBounds(), { padding: [50, 50] });
    }

    const handleVehicleChange = (e) => {
        const vehicleId = e.target.value;
        setSelectedVehicle(vehicleId);

        if (vehicleId) {
            api.get(`/route/vehicle?{licensePlate}`)
                .then(res => setRoutes(res.data))
                .catch(err => console.error(err));
        } else {
            setRoutes([]);
        }
    };

    const displayRouteOnMap = (route) => {
        if (!mapRef.current || !route.coords) return;

        const waypoints = route.coords.map((c) => L.latLng(c.lat, c.lon));

        if (routingControlRef.current) {
            routingControlRef.current.setWaypoints(waypoints);
        } else {
            routingControlRef.current = L.Routing.control({
                waypoints,
                router: L.Routing.osrmv1({
                    serviceUrl: "http://localhost:8080/osrm/route/v1",
                }),
                createMarker: (i, wp) => L.marker(wp.latLng, { icon: customIcon }),
            }).addTo(mapRef.current);
        }
    };


    return (
        <>
            <div className="container mt-4">
                <h3>Vehicle Dashboard</h3>

                <div className="mb-3">
                    <label htmlFor="vehicle" className="form-label">Select Vehicle</label>
                    <select
                        id="vehicle"
                        className="form-select"
                        value={selectedVehicle}
                        onChange={handleVehicleChange}
                    >
                        <option value="">-- Choose Vehicle --</option>
                            {vehicles.map((v) => (
                                <option key={v.id} value={v.id}>
                                    {v.name}
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
                                <th>Finish</th>
                                <th>Distance (km)</th>
                                <th>Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            {routes.map((r) => (
                                <tr key={r.id}>
                                    <td>{r.id}</td>
                                    <td>{r.start}</td>
                                    <td>{r.finish}</td>
                                    <td>{r.distance}</td>
                                    <td>{new Date(r.createdAt).toLocaleString()}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                ) : (
                    selectedVehicle && <p>No routes available for this vehicle.</p>
                )}
            </div>

            {locations.map((loc) => (
                <div key={loc.id} className="mb-3 border p-2 rounded">
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
                                <button className="btn btn-primary mt-2 mr-4" onClick={() => handleStructuredSearch(loc.id)}>
                                    Structured Search
                                </button>
                    <button className="btn btn-danger mt-2" onClick={() => removeLocation(loc.id)}>
                        Remove Location
                    </button>
                </div>
            ))}

            <button className="btn btn-success m-3" onClick={addLocation}>
                Add Location
            </button>

            <div id="map"/>
        </>
    );
}

export default Route;