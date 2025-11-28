import { Navigate } from "react-router-dom";

function PublicRoute({ user, children }) {
    if (user) {
        //when logged in, redirect to dashboard
        return <Navigate to="/" replace />;
    }
    return children;
}
export default PublicRoute