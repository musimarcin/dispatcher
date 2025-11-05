import { useState } from "react";
import Vehicles from "../assets/Vehicles"
import Notifications from "../assets/Notifications"
import "bootstrap/dist/css/bootstrap.min.css";

function Dashboard({showToast}) {
    const [tab, setTab] = useState("vehicles");

    return (
        <div className="container mt-4">
            <h2 className="mb-3">Dashboard</h2>

            <ul className="nav nav-tabs mb-3">
            <li className="nav-item">
            <button
                className={`nav-link ${tab === "vehicles" ? "active" : ""}`}
                onClick={() => setTab("vehicles")}
            >
                Vehicles
            </button>
            </li>

            <li className="nav-item">
            <button
                className={`nav-link ${tab === "notifications" ? "active" : ""}`}
                onClick={() => setTab("notifications")}
            >
                Notifications
            </button>
            </li>
            </ul>

            {tab === "vehicles" && (
                <Vehicles showToast={showToast} />
            )}

            {tab === "notifications" && (
                <Notifications showToast={showToast} />
            )}
        </div>
    );
}
export default Dashboard;
