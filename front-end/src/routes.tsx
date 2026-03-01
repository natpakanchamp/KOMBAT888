import {createBrowserRouter} from "react-router";
import MainLayout from "./layout/MainLayout.tsx";
import HomePage from './pages/HomePage';
import ErrorPage from './pages/ErrorPage';
import SelectMinionsPage from "./pages/SelectMinionsPage.tsx";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <MainLayout/>,
        errorElement: <ErrorPage/>,
        children: [
            {index: true, element: <HomePage/>},
            {path: "select", element: <SelectMinionsPage/>}
        ],
    }
])