import submits from "./submit.js"

//const API_BASE_URL = "http://localhost:9000/"

const API_BASE_URL = "https://service-ls-2223-2-42d-g11.onrender.com/"

export default async function dataFetch(path, method = "GET", body=undefined) {
    const options = {
        method : method,
        headers : {
            "Content-Type" : "application/json",
            "Accept" : "application/json",
            "Authorization": "Bearer " + submits.token || ""
        },
        body: body
    }
    return await fetch(API_BASE_URL + path, options)
        .then(res => res.json())
}
