///* global L */
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { useEffect } from 'react';

function Route() {
    useEffect(() => {
        const map = L.map('map').setView([40.7128, -74.0060], 12); // New York

  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);

        return () => { map.remove(); };

    }, []);

    return (
        <div id="map" style={{ height: '100%', width: '100%' }}/>
    );
}

export default Route;