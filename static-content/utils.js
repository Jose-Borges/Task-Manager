function createSelect(label, id, options = undefined) {
    const selectLabel = document.createElement("label")
    selectLabel.appendChild(document.createTextNode(label))
    const select = document.createElement("select")
    select.id = id
    if (options !== undefined) {
        const defaultOption = document.createElement('option')
        defaultOption.text = 'Select an option'
        defaultOption.value = ''
        defaultOption.selected = true
        select.appendChild(defaultOption)
        options.forEach(o => {
            const option = document.createElement("option")
            option.value = o.id
            option.text = o.name
            select.appendChild(option)
        })
    }
    return { label: selectLabel, select: select }
}

function updateSelect(options) {
    const firstSelect = document.getElementById("idFirstSelect")
    const secondSelect = document.getElementById("idSecondSelect")
    const selectedValue = firstSelect.value
    secondSelect.innerHTML = ''
    let i = 1
    const cards = options.find(o => o.id == selectedValue).cards
    cards.forEach(() => {
        const option = document.createElement('option')
        option.text = `${i}`
        option.value = `${i}`
        secondSelect.appendChild(option)
        i++
    })
    const option = document.createElement('option')
    option.text = `${i}`
    option.value = `${i}`
    secondSelect.appendChild(option)
}

function createDiv(txt) {
    const div = document.createElement("div")
    const h1 = document.createElement("h1")
    const text = document.createTextNode(txt)
    h1.appendChild(text)
    div.appendChild(h1)
    return div
}

function createInput(label, type, id, value, required = false) {
    const input = document.createElement("input")
    input.type = type
    input.id = id
    input.required = required
    input.style.backgroundColor = "#D3D3D3"
    input.style.fontSize = "16px"
    input.style.border = "1px solid black"
    input.style.outline = "none"

    if (value !== undefined) input.value = value
    if(!label) {
        return input
    } else {
        const inputLabel = document.createElement("label")
        inputLabel.appendChild(document.createTextNode(label))
        return { label: inputLabel, input: input }
    }
}

function createForm(elements) {
    const form = document.createElement("form")
    elements.every(it=> form.appendChild(it))
    return form
}

function createInputTextArea(label, id, required) {
    const container = document.createElement("div")
    container.classList.add("form-group")

    const labelContainer = document.createElement("label")
    labelContainer.style.display = "block"
    labelContainer.textContent = label

    const input = document.createElement("textarea")
    input.id = id
    input.required = required
    input.classList.add("text-area-input")
    input.style.resize = "none"
    input.style.backgroundColor = "#D3D3D3"
    input.style.fontSize = "16px"
    input.style.border = "1px solid black"
    input.style.outline = "none"

    container.appendChild(labelContainer)
    container.appendChild(input)

    return { label: labelContainer, input: input, container: container }
}



function createParagraph(elements) {
    const p = document.createElement("p")
    elements.every(it => p.appendChild(it))
    return p
}

function createButton(name = "Submit") {
    const button = document.createElement("input")
    button.type = "submit"
    button.value = name
    button.style.backgroundColor = "#D3D3D3"
    button.style.fontSize = "16px"
    button.style.borderRadius = "5px"
    button.style.cursor = "pointer"
    button.style.border = "1px solid black"
    button.style.boxShadow = "0 2px 4px rgba(0, 0, 0, 0.3)"
    return button
}

function createPreviousAndNextButtons(path, length) {
    const previous = utils.createButton("Previous")
    const skip = parseInt(path.match(/skip=(\d+)/)[0].split("=")[1])
    const limit = parseInt(path.match(/limit=(\d+)/)[0].split("=")[1])
    previous.onclick = () => {
        let newSkip = skip - limit
        if (newSkip < 0) newSkip = 0
        window.location.hash = path.replace(/skip=(\d+)/, `skip=${newSkip}`)
    }
    if(skip === 0) previous.disabled = true
    const next = utils.createButton("Next")
    next.onclick = () => {
        const newSkip = skip + limit
        window.location.hash = path.replace(/skip=(\d+)/, `skip=${newSkip}`)
    }
    if (length < limit) next.disabled = true
    return [previous, next]
}

function createElementList(txt, array) {
    const li = document.createElement("li")
    const p = document.createElement("p")
    li.appendChild(p.appendChild(document.createTextNode(txt)))
    li.appendChild(ul(array))
    return li
}

function liWithRef(txt, href) {
    const li = document.createElement("li")
    const l = document.createElement("a")
    l.appendChild(document.createTextNode(txt))
    l.href= href
    li.appendChild(l)
    return li
}

function li(txt) {
    const liName = document.createElement("li")
    const textName = document.createTextNode(txt)
    liName.appendChild(textName)
    return liName
}

function ul(li) {
    const ulUser = document.createElement("ul")
    li.every(it=> ulUser.appendChild(it))
    return ulUser
}

function checkError(res) {
    if (res.status != undefined) throw { status: res.status, message: res.message }
}

export const utils = {
    createSelect: createSelect,
    updateSelect: updateSelect,
    createDiv: createDiv,
    createInput: createInput,
    createForm: createForm,
    createInputTextArea: createInputTextArea,
    createParagraph: createParagraph,
    createButton: createButton,
    createPreviousAndNextButtons: createPreviousAndNextButtons,
    createElementList: createElementList,
    liWithRef: liWithRef,
    li: li,
    ul: ul,
    checkError: checkError
}

export default utils