import { useEffect, useState } from "react";
import api from "../assets/api";

function Notifications({showToast}) {

    const [notifications, setNotifications] = useState([]);
    const [selectedNotification, setSelectedNotification] = useState(null);
    const [notificationPage, setNotificationPage] = useState(0);
    const [notificationTotalPages, setNotificationTotalPages] = useState(0);

    useEffect(() => {
        api
        .get(`/notifications?page=${notificationPage + 1}`)
        .then((response) => {
            setNotifications(response.data.notificationDtoList);
            setNotificationTotalPages(response.data.totalPages);
            setSelectedNotification(null); // reset selection on page change
        })
        .catch((err) => showToast(err.response?.data || err.message, "error"));
    }, [notificationPage]);

    const selectNotification = (notification) => {
        api.post('/notifications/read', notification);
        setSelectedNotification(notification);
    }

    return (
    <>
        <ul className="list-group mb-4">
            {notifications.length === 0 && <li>No notifications found.</li>}

            {notifications.map((notification) => (
                <li
                    key={notification.id}
                    className={`list-group-item ${
                        selectedNotification?.id === notification.id ? "active" : ""
                    } ${notification.isRead ? "" : "fw-bold"}`}
                    onClick={() => selectNotification(notification)}
                    style={{ cursor: "pointer" }}
                >
                    {notification.message}

                    <br />
                    <small className="text-muted">
                        {new Date(notification.createdAt).toLocaleString("pl-PL")}
                    </small>
                </li>
            ))}
        </ul>

        <div className="d-flex justify-content-center align-items-center mb-3">
            <button
                className="btn btn-outline-primary me-2"
                disabled={notificationPage === 0}
                onClick={() => setNotificationPage(notificationPage - 1)}
            >
                Prev
            </button>
                <span>
                    Page {notificationPage + 1} of {notificationTotalPages}
                </span>
            <button
                className="btn btn-outline-primary ms-2"
                disabled={notificationPage + 1 === notificationTotalPages}
                onClick={() => setNotificationPage(notificationPage + 1)}
            >
                Next
            </button>
        </div>

        {selectedNotification && (
            <div className="card">
                <div className="card-body">
                    <p>{selectedNotification.message}</p>
                    <p>
                        <small className="text-muted">
                            Created:{" "}
                            {new Date(selectedNotification.createdAt).toLocaleString("pl-PL")}
                        </small>
                    </p>
                    <p>User ID: {selectedNotification.userId}</p>
                    <p>Status: {selectedNotification.isRead ? "Read" : "Unread"}</p>
                </div>
            </div>
        )}
        </>
    )
}
export default Notifications;