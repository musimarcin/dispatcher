import { useEffect, useRef } from "react";
import { Toast } from "bootstrap";

function ToastMessage({ message, type = "success", show, onClose }) {
    const toastRef = useRef();

    useEffect(() => {
        if (show && toastRef.current) {
            const bsToast = new Toast(toastRef.current, { delay: 3000 });
            bsToast.show();

            toastRef.current.addEventListener("hidden.bs.toast", () => {
                onClose && onClose();
            });
        }
    }, [show, onClose]);

    const bgColor =
        type === "success"
            ? "bg-success text-white"
            : type === "error"
            ? "bg-danger text-white"
            : "bg-info text-white";

    return (
    <div className="toast-container position-fixed top-0 end-0 p-3" style={{ zIndex: 9999 }}>
        <div ref={toastRef} className={`toast align-items-center ${bgColor}`} role="alert">
            <div className="d-flex">
                <div className="toast-body">{message}</div>
                    <button
                        type="button"
                        className="btn-close btn-close-white me-2 m-auto"
                        data-bs-dismiss="toast"
                        aria-label="Close"
                ></button>
            </div>
        </div>
    </div>
    );
}
export default ToastMessage;