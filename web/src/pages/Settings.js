import { useState, useEffect } from "react";
import api from '../assets/api'
import { useNavigate } from 'react-router-dom';

function Settings() {
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
            api.get("/auth/roles")
            .then(response => {
                setRoles(response.data);
                localStorage.setItem("roles", JSON.stringify(response.data));
            }).catch(err => alert(err.response?.data)); //question mars to check if previous part returned null
        }

        const storedUserRoles = localStorage.getItem("userRoles");

        if (storedUserRoles) {
            const userRolesData = JSON.parse(storedUserRoles);
            setUserRoles(userRolesData);
            //filter out roles
            const allRoles = JSON.parse(storedRoles);
            const filtered = allRoles.filter(role => !userRolesData.includes(role));
            setRoles(filtered);
        } else {
            updateRoles()
                .then(() => { //filter out roles
                    const allRoles = JSON.parse(localStorage.getItem("roles") || "[]");
                    const userRolesData = JSON.parse(localStorage.getItem("userRoles") || "[]");
                    const filtered = allRoles.filter(role => !userRolesData.includes(role));
                    setRoles(filtered);
                });
        }
    }, []);

    const updateRoles = () => {
        return api.get("/auth/role/user")
            .then(response => {
                setUserRoles(response.data);
                localStorage.setItem("userRoles", JSON.stringify(response.data));
                //filter out roles
                const allRoles = JSON.parse(localStorage.getItem("roles") || "[]");
                const filtered = allRoles.filter(role => !response.data.includes(role));
                setRoles(filtered);
                setSelectedAvailableRoles([]);
            }).catch(err => alert(err.response?.data));
    };

    const handleUserRolesChange = (e) => {
        const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
        setSelectedUserRoles(selectedOptions);
    };

    const handleAvailableRolesChange = (e) => {
        const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
        setSelectedAvailableRoles(selectedOptions);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        api.put("/auth/change", {
            username,
            password,
            email
            }).then(navigate('/settings'))
            .catch(err => alert(err.response?.data));
    };

    const removeRoles = async (e) => {
        e.preventDefault();
        const rolesWithPrefix = selectedUserRoles.map(role => "ROLE_" + role);

        api.post("/auth/change/role/remove", {
            roles: rolesWithPrefix
        }).then(() => {
            return updateRoles();
        }).then(() => {
            setSelectedUserRoles([]); //refreshes window
        }).catch(err => alert(err.response?.data));
    };

    const addRoles = async (e) => {
        e.preventDefault();
        const rolesWithPrefix = selectedAvailableRoles.map(role => "ROLE_" + role);

        api.post("/auth/change/role/add", {
            roles: rolesWithPrefix
        }).then(() => {
            return updateRoles();
        }).then(() => {
            setSelectedAvailableRoles([]); //refreshes window
        }).catch(err => alert(err.response?.data));
    };

    const deleteUser = async (e) => {
        await api.delete('auth/delete')
        .then(response => {
            alert(response.data);
            navigate('/login');
            localStorage.clear();
        })
        .catch(err => alert(err.response?.data));
    }

    return (
        <div>
            <h2>Settings</h2>
            <form onSubmit={handleSubmit}>

                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <input
                    type="email"
                    placeholder="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <button type="button" onClick={handleSubmit}>
                    Change
                </button>

                <h3>User roles</h3>
                <select
                    multiple
                    onChange={handleUserRolesChange}
                    value={selectedUserRoles}
                >
                    {userRoles.map(role => (
                        <option key={role} value={role}>{role}</option>
                    ))}
                </select>
                <button type="button" onClick={removeRoles}>
                    Remove Role
                </button>

                <h3>Available roles</h3>
                <select
                    multiple
                    onChange={handleAvailableRolesChange}
                    value={selectedAvailableRoles}
                >
                    {roles.map(role => (
                        <option key={role} value={role}>{role}</option>
                    ))}
                </select>
                <button type="button" onClick={addRoles}>
                    Add Role
                </button>
            </form>
            <button type="button" onClick={() => setIsPopUp(true)}>
                Delete User
            </button>

            {isPopUp && (
                <div>
                    <div>
                        <h2>User Deletion</h2>
                        <p>Are you sure?</p>
                        <button onClick={() => {
                                    setIsPopUp(false)
                                    deleteUser();
                                }
                            }>
                            Yes
                        </button>
                        <button onClick={() => setIsPopUp(false)}>
                            No
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Settings;
