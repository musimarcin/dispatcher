import { useState, useEffect, useContext } from "react";
import { AuthContext } from "../assets/AuthContext";
import api from '../assets/api'
import { useNavigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';

function Settings({showToast}) {

    const { user } = useContext(AuthContext);

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [email, setEmail] = useState("");
    const [roles, setRoles] = useState([]);
    const [selectedUserRoles, setSelectedUserRoles] = useState([]);
    const [selectedAvailableRoles, setSelectedAvailableRoles] = useState([]);
    const [userRoles, setUserRoles] = useState([]);
    const [availableRoles, setAvailableRoles] = useState([]);
    const [isPopUp, setIsPopUp] = useState(false);
    const navigate = useNavigate();
    const [users, setUsers] = useState([]);
    const [selectedUser, setSelectedUser] = useState([]);

    useEffect(() => {
        api.get("/roles")
        .then(response => {
            if (!response.data.body) {
                showToast(response.data.message, "error");
                return;
            }
            setRoles(response.data.body);
            setAvailableRoles(response.data.body)
        }).catch(err => console.log(err));

        if (user.roles.includes("ADMIN")) {
            api.get("/user")
            .then(response => {
            if (!response.data.body) {
                showToast(response.data.message, "error");
                return;
            }
            setUsers(response.data.body)
            }).catch(err => console.log(err))
        }

    }, []);

    const handleUserRolesChange = (e) => {
        const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
        setSelectedUserRoles(selectedOptions);
    };

    const handleAvailableRolesChange = (e) => {
        const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
        setSelectedAvailableRoles(selectedOptions);
    };

    const changeUsername = async (e) => {
        e.preventDefault();

        api.put("/user/username",
            { newUsername: username }
        ).then(res => {
            showToast(res.data.message, "success")
            setUsername("")
        }).catch(err => showToast(err.response?.data.message, "error"));
    }

    const changePassword = async (e) => {
        e.preventDefault();

        api.put("/user/password",
            { newPassword: password }
        ).then(res => {
            showToast(res.data.message, "success")
            setPassword("")
        }).catch(err => showToast(err.response?.data.message, "error"));
    }

    const changeEmail = async (e) => {
        e.preventDefault();

        api.put("/user/email",
            { newEmail: email }
        ).then(res => {
            showToast(res.data.message, "success")
            setEmail("")
        }).catch(err => showToast(err.response?.data.message, "error"));
    }

    const removeRoles = async (e) => {
        e.preventDefault();

        api.patch("/roles/remove", {
            roles: selectedUserRoles,
            username:  selectedUser.username
        }).then(res => {
            const filtered = userRoles.filter(role => !selectedUserRoles.includes(role))
            setUserRoles(filtered)
            setAvailableRoles(availableRoles.concat(selectedUserRoles));
            setSelectedUserRoles([]); //refreshes window
            showToast(res.data.message, "success");
        }).catch(err => showToast(err.res?.data.message, "error"));
    };

    const addRoles = async (e) => {
        e.preventDefault();

        api.patch("/roles/add", {
           roles: selectedAvailableRoles,
           username:  selectedUser.username
        }).then(response => {
            setUserRoles(userRoles.concat(selectedAvailableRoles))
            const filtered = availableRoles.filter(role => !selectedAvailableRoles.includes(role))
            setAvailableRoles(filtered);
            setSelectedAvailableRoles([]); //refreshes window
            showToast(response.data.message, "success");
        }).catch(err => showToast(err.response?.data.message, "error"));
    };

    const deleteUser = async (e) => {
        await api.delete('/user')
        .then(response => {
            showToast(response.data.message, "success");
            navigate('/login');
        })
        .catch(err => showToast(err.response?.data.message, "error"));
    }

    const handleUserChange = (e) => {
        const userId = e.target.value
        if (!userId) {
            setSelectedUser([]);
            setUserRoles([]);
            setAvailableRoles([]);
            return;
        }

        const user = users.find(u => u.id == userId);
        setSelectedUser(user);

        api.get(`/roles/${user.id}`)
            .then(response => {
                const res = response.data.body;
                if (res == null) {
                    showToast(response.data.message, "error")
                    return;
                }
                setUserRoles(res || []);
                setAvailableRoles(roles.filter(role => !res.includes(role)));
                setSelectedAvailableRoles([]);
            }).catch(err => console.log(err));
    }

    return (
        <div className="container mt-4">
            <h2 className="mb-4">Settings</h2>
            <div className="mb-1">
                <input
                    type="text"
                    className="form-control"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
            </div>
            <button className="btn btn-primary mb-2" onClick={changeUsername}>
                Change Username
            </button>
            <div className="mb-1">
                <input
                    type="password"
                    className="form-control"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
            </div>
            <button className="btn btn-primary mb-2" onClick={changePassword}>
                Change Password
            </button>
            <div className="mb-1">
                <input
                    type="email"
                    className="form-control"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
            </div>
            <button className="btn btn-primary mb-2" onClick={changeEmail}>
                Change Email
            </button>

            <button className="btn btn-outline-danger my-2" onClick={() => setIsPopUp(true)}>
                Delete User
            </button>

            {isPopUp && (
                <div className="modal show d-block" tabIndex="-1">
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">Confirm Deletion</h5>
                                <button
                                    type="button"
                                    className="btn-close"
                                    onClick={() => setIsPopUp(false)}
                                ></button>
                            </div>
                            <div className="modal-body">
                                <p>Are you sure you want to delete your account?</p>
                            </div>
                            <div className="modal-footer">
                                <button
                                    className="btn btn-danger"
                                    onClick={() => {
                                        setIsPopUp(false);
                                        deleteUser();
                                    }}
                                >
                                    Yes, delete
                                </button>
                                <button
                                    className="btn btn-secondary"
                                    onClick={() => setIsPopUp(false)}
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {user.roles.includes("ADMIN") && (
            <>
                <div>
                    <label htmlFor="user" className="form-label">Select User</label>
                    <select
                        id="user"
                        className="form-select"
                        value={selectedUser?.id || ""}
                        onChange={handleUserChange}
                    >
                        <option value="">-- Choose User --</option>
                            {users.map((u) => (
                                <option key={u.id} value={u.id}>
                                    {u.id}. {u.username}
                                </option>
                            ))}
                    </select>
                </div>
                <div className="row mb-4">
                    <div className="col-md-6">
                        <h4>User Roles</h4>
                        <select
                            className="form-select"
                            multiple
                            onChange={handleUserRolesChange}
                            value={selectedUserRoles}
                            size={5}
                        >
                            {userRoles.map((role) => (
                                <option key={role} value={role}>{role}</option>
                            ))}
                        </select>
                        <button className="btn btn-danger mt-2" onClick={removeRoles}>
                            Remove Selected Roles
                        </button>
                    </div>

                    <div className="col-md-6">
                        <h4>Available Roles</h4>
                        <select
                            className="form-select"
                            multiple
                            onChange={handleAvailableRolesChange}
                            value={selectedAvailableRoles}
                            size={5}
                        >
                            {availableRoles.map((role) => (
                                <option key={role} value={role}>{role}</option>
                            ))}
                        </select>
                        <button className="btn btn-success mt-2" onClick={addRoles}>
                            Add Selected Roles
                        </button>
                    </div>
                </div>
            </>
            )}
        </div>
    );
}

export default Settings;
