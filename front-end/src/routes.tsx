import {createBrowserRouter} from "react-router";
import MainLayout from "./layout/MainLayout.tsx";
import HomePage from './pages/HomePage';
import ErrorPage from './pages/ErrorPage';
import LoginPage from './pages/LoginPage';
import SelectMinionsPage from "./pages/SelectMinionsPage.tsx";
// import WaitingRoomPage from "./pages/WaitingRoomPage.tsx";
// import BattlePage from "./pages/BattlePage.tsx";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <MainLayout/>,
        errorElement: <ErrorPage/>,
        children: [
            {index: true, element: <HomePage/>},
            {path: "login", element: <LoginPage/>},
            {path: "select", element: <SelectMinionsPage/>},
            // {path: "battle", element: <BattlePage/>},
            // {path: "waitingRoom", element: <WaitingRoomPage/>}
        ],
    }
])