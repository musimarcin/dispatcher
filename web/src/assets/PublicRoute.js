import { Navigate } from "react-router-dom";

function PublicRoute({ isLoggedIn, children }) {
    if (isLoggedIn) {
        //when logged in, redirect to dashboard
        return <Navigate to="/" replace />;
    }
    return children;
}
export default PublicRoute