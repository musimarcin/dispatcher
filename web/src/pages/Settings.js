import { useState, useEffect } from "react";
import api from '../assets/api'
import { useNavigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';

function Settings({showToast}) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [email, setEmail] = useState("");
    const [roles, setRoles] = useState([]);
    const [selectedUserRoles, setSelectedUserRoles] = useState([]);
    const [selectedAvailableRoles, setSelectedAvailableRoles] = useState([]);
    const [userRoles, setUserRoles] = useState([]);
    const [isPopUp, setIsPopUp] = useState(false);
    const navigate = useNavigate();


    useEffect(() => {
        const storedRoles = localStorage.getItem("roles");
        if (storedRoles) {
            setRoles(JSON.parse(storedRoles))
        } else {
            api.get("/user/roles")
            .then(response => {
                setRoles(response.data.body);
                localStorage.setItem("roles", JSON.stringify(response.data.body));
            }).catch(err => showToast("Failed to fetch roles", "error")); //question marks to check if previous part returned null
        }

        api.get("/user/roles/me")
            .then(response => {
            console.log(response)
                setUserRoles(response.data.body);
                const allRoles = JSON.parse(localStorage.getItem("roles") || "[]");
                const userRolesData = JSON.parse(userRoles || "[]");
                const filtered = allRoles.filter(role => !userRolesData.includes(role));
                setRoles(filtered);
                setSelectedAvailableRoles([]);
            }).catch(err => showToast(err.response?.data.message, "error"));

    }, []);

    const updateRoles = () => {
        return api.get("/user/roles/me")
            .then(response => {
            console.log(response)
                setUserRoles(response.data.body);
                const allRoles = JSON.parse(localStorage.getItem("roles") || "[]");
                const userRolesData = JSON.parse(userRoles || "[]");
                const filtered = allRoles.filter(role => !userRolesData.includes(role));
                setRoles(filtered);
                setSelectedAvailableRoles([]);
            }).catch(err => showToast(err.response?.data.message, "error"));
    };

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
        const rolesWithPrefix = selectedUserRoles.map(role => "ROLE_" + role);

        api.patch("/user/roles/remove",
            { roles: rolesWithPrefix }
        ).then(() => {
            return updateRoles();
        }).then(res => {
            setSelectedUserRoles([]); //refreshes window
            showToast(res.data.message, "success");
        }).catch(err => showToast(err.res?.data.message, "error"));
    };

    const addRoles = async (e) => {
        e.preventDefault();
        const rolesWithPrefix = selectedAvailableRoles.map(role => "ROLE_" + role);

        api.patch("/user/roles/add",
            { roles: rolesWithPrefix }
        ).then(response => {
            return updateRoles();
        }).then(response => {
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
                        {roles.map((role) => (
                            <option key={role} value={role}>{role}</option>
                        ))}
                    </select>
                    <button className="btn btn-success mt-2" onClick={addRoles}>
                        Add Selected Roles
                    </button>
                </div>
            </div>

            <button className="btn btn-outline-danger" onClick={() => setIsPopUp(true)}>
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
        </div>
    );
}

export default Settings;
