import React, { useState } from "react";

const FileUploader = () => {
    const [file, setFile] = useState(null);
    const [message, setMessage] = useState("");

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
    };

    const handleUpload = async () => {
        if (!file) {
            alert("Please select a file");
            return;
        }

        const formData = new FormData();
        formData.append("file", file);

        try {
            const response = await fetch("http://localhost:8080/api/files/upload", {
                method: "POST",
                body: formData,
            });

            const result = await response.text();
            setMessage(result);
        } catch (error) {
            setMessage("Upload failed");
        }
    };

    return (
        <div>
            <input type="file" onChange={handleFileChange} />
            <button onClick={handleUpload}>Upload</button>
            {message && <p>{message}</p>}
        </div>
    );
};

export default FileUploader;
