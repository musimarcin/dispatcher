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
            }).catch(err => alert(err.response?.data?.message)); //question mars to check if previous part returned null
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
            }).catch(err => alert(err.response?.data?.message));
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
            }).catch(err => alert(err.response?.data?.message));
        navigate('/settings');
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
        }).catch(err => alert(err.response?.data?.message));
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
        }).catch(err => alert(err.response?.data?.message));
    };

    return (
        <div className="max-w-md mx-auto mt-20 p-4 border rounded-lg shadow-lg">
            <h2 className="text-2xl mb-4">Settings</h2>
            <form onSubmit={handleSubmit}>

                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    className="w-full p-2 mb-4 border rounded"
                    required
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full p-2 mb-4 border rounded"
                    required
                />

                <input
                    type="email"
                    placeholder="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full p-2 mb-4 border rounded"
                    required
                />

                <button type="button" onClick={handleSubmit} className="p-20 w-full p-2 bg-blue-500 text-white rounded hover:bg-blue-600">
                    Change
                </button>

                <h3>User roles</h3>
                <select
                    multiple
                    onChange={handleUserRolesChange}
                    value={selectedUserRoles}
                    className="w-full p-2 mb-4 border rounded"
                >
                    {userRoles.map(role => (
                        <option key={role} value={role}>{role}</option>
                    ))}
                </select>
                <button type="button" onClick={removeRoles} className="p-20 w-full p-2 bg-blue-500 text-white rounded hover:bg-blue-600">
                    Remove Role
                </button>

                <h3>Available roles</h3>
                <select
                    multiple
                    onChange={handleAvailableRolesChange}
                    value={selectedAvailableRoles}
                    className="w-full p-2 mb-4 border rounded"
                >
                    {roles.map(role => (
                        <option key={role} value={role}>{role}</option>
                    ))}
                </select>
                <button type="button" onClick={addRoles} className="p-20 w-full p-2 bg-blue-500 text-white rounded hover:bg-blue-600">
                    Add Role
                </button>

            </form>
        </div>
    );
}

export default Settings;
