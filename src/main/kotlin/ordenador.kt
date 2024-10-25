import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import javax.swing.*
import java.awt.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class YAMLOrderer : JFrame() {
    private var selectedFile: File? = null
    private var selectedOrderFile: File? = null // File for ordering subkeys

    private val textArea = JTextArea()
    private val keyToOrderField = JTextField(20)
    private val scrollPane = JScrollPane(textArea)
    private val menuBar = JMenuBar()

    private val fileMenu = JMenu("File")
    private val editMenu = JMenu("Edit")
    private val helpMenu = JMenu("Help")

    private val openMenuItem = JMenuItem("Open")
    private val saveMenuItem = JMenuItem("Save")
    private val saveAsMenuItem = JMenuItem("Save As...")
    private val sortMenuItem = JMenuItem("Sort")
    private val exitMenuItem = JMenuItem("Exit")
    private val aboutMenuItem = JMenuItem("About YAML Key Sorter 2000")

    private val fileChooser = JFileChooser()

    // Panel for key and order subkeys selection
    private val inputPanel = JPanel()

    init {
        setupFrame()
        setupMenu()
        setupInputPanel()
    }

    private fun setupFrame() {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = BorderLayout()
        title = "YAML Key Sorter 2000"
        add(scrollPane)
        textArea.isEditable = true
        setSize(750, 400)
        isVisible = true
    }

    private fun setupMenu() {
        setJMenuBar(menuBar)
        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(helpMenu)

        fileMenu.add(openMenuItem)
        fileMenu.add(saveMenuItem)
        fileMenu.add(saveAsMenuItem)
        fileMenu.add(JSeparator())
        fileMenu.add(exitMenuItem)

        editMenu.add(sortMenuItem)

        helpMenu.add(aboutMenuItem)
        aboutMenuItem.addActionListener { showAboutDialog() }

        openMenuItem.addActionListener { openFile() }
        saveMenuItem.addActionListener { saveFile(selectedFile) }
        saveAsMenuItem.addActionListener { saveAsFile() }
        sortMenuItem.addActionListener { sortYAML() }
        exitMenuItem.addActionListener { exitApplication() }
    }

    private fun setupInputPanel() {
        inputPanel.layout = FlowLayout(FlowLayout.LEFT)
        inputPanel.add(JLabel("Key to Order:"))
        inputPanel.add(keyToOrderField)

        val selectOrderButton = JButton("Choose Subkey Order File")
        selectOrderButton.addActionListener { selectOrderFile() }
        inputPanel.add(selectOrderButton)

        add(inputPanel, BorderLayout.NORTH) // Add the input panel at the top
    }

    private fun openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.selectedFile
            textArea.text = readFileContent(selectedFile!!)
        }
    }

    private fun saveFile(file: File?) {
        // Check if there is no file opened
        if (selectedFile == null) {
            showMessage("Error: No file is currently opened. Please open a file before saving.")
            return
        }

        file?.let { writeFileContent(it, textArea.text) }
    }

    private fun saveAsFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            writeFileContent(fileChooser.selectedFile, textArea.text)
        }
    }

    private fun writeFileContent(file: File, content: String) {
        FileWriter(file).use { it.write(content) }
    }

    private fun readFileContent(file: File): String {
        return FileReader(file).use { reader ->
            reader.readText()
        }
    }

    private fun selectOrderFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedOrderFile = fileChooser.selectedFile
            showMessage("Selected: ${selectedOrderFile?.absolutePath}")
        }
    }

    private fun sortYAML() {
        val keyToOrder = keyToOrderField.text.trim()
        if (keyToOrder.isEmpty() || selectedOrderFile == null) {
            showMessage("Please enter a key and select a subkey order file.")
            return
        }

        val orderSubkeys = selectedOrderFile!!.readLines().map { it.trim() }
        val yamlContent = textArea.text
        val yaml = Yaml()

        try {
            val data = yaml.load<Map<String, Any>>(yamlContent) ?: emptyMap()
            val newData = data.toMutableMap()

            findAndSort(newData, keyToOrder, orderSubkeys)

            val newYAMLContent = convertMapToYAML(newData)
            textArea.text = newYAMLContent

        } catch (e: Exception) {
            showMessage("Error processing the YAML: ${e.message}")
        }
    }

    private fun findAndSort(map: MutableMap<String, Any>, keyToOrder: String, orderSubkeys: List<String>) {
        for ((key, value) in map) {
            if (value is Map<*, *>) {
                findAndSort(value as MutableMap<String, Any>, keyToOrder, orderSubkeys)
            }
            if (key == keyToOrder) {
                map[key] = orderSubkeysInSection(value as? Map<String, Any>, orderSubkeys)
            }
        }
    }

    private fun orderSubkeysInSection(section: Map<String, Any>?, orderSubkeys: List<String>): Map<String, Any> {
        val orderedSubkeys = mutableMapOf<String, Any>()
        section?.let {
            for (key in orderSubkeys) {
                it[key]?.let { value -> orderedSubkeys[key] = value }
            }
        }
        return orderedSubkeys
    }

    private fun convertMapToYAML(data: Map<String, Any>): String {
        val options = DumperOptions().apply {
            indent = 2
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        }
        val yamlWithOrder = Yaml(options)
        return yamlWithOrder.dump(data)
    }

    private fun exitApplication() {
        System.exit(0)
    }

    private fun showAboutDialog() {
        val message = """
            YAML Key Sorter 2000 is a simple application that allows you to organize subkeys in a YAML file.
            To use it, follow these steps:
            1. Open a YAML file that contains the data you want to sort.
            2. Enter the key that you want to organize in the 'Key to Order' field.
            3. Select a text file that contains the order of the subkeys you wish to apply.
               Each key should be on a separate line in the order you want them sorted.
            4. Click the 'Sort' button to reorder the subkeys according to your specified order.
            5. Save the changes to the YAML file.

            Enjoy organizing your YAML data!
        """.trimIndent()

        JOptionPane.showMessageDialog(this, message, "About YAML Key Sorter 2000", JOptionPane.INFORMATION_MESSAGE)
    }

    private fun showMessage(message: String) {
        JOptionPane.showMessageDialog(this, message)
    }
}

fun main(args: Array<String>) {
    EventQueue.invokeLater { YAMLOrderer() }
}
