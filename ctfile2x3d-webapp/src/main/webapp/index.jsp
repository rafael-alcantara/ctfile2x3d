<html>
    <head>
        <meta charset="utf-8">
        <link rel="stylesheet" href="x3dom-1.5.1/x3dom.css">
        <script src="x3dom-1.5.1/x3dom-full.js"></script>
        <style>
            #x3d { width: 100% !important; height: 500px !important; }
            x3d { width: 100% !important; height: 400px !important; }
        </style>
    </head>
    <body>
        <form action="ctfile2x3d?id" target="x3d">
            <label for="id">Compound/reaction ID:</label>
            <input type="text" id="id">
            <br>
            <label for="fomrat">Format:</label>
            <select id="format">
                <option value="mol">MOL</option>
                <option value="rxn">RXN</option>
            </select>
            <br>
            <button type="submit">View as X3D</button>
        </form>
        
        <iframe id="x3d" src="about:blank">

    </body>
</html>