import React from "react";
import ReactDom from "react-dom/client";
import {RouterProvider} from "react-router-dom";
import {router} from "./routes.tsx";
import "@mantine/core/styles.css";
import {MantineProvider} from "@mantine/core";
import "./index.css";


ReactDom.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <MantineProvider>
            <RouterProvider router={router} />
        </MantineProvider>
    </React.StrictMode>
);