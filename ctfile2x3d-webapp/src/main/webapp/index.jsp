<%@page pageEncoding="UTF-8" %>
<html>
    <head>
        <meta charset="utf-8">
        <link rel="stylesheet" href="x3dom-1.6.0/x3dom.css">
        <script src="x3dom-1.6.0/x3dom-full.js"></script>
        <style>
            #x3d { width: 100% !important; height: 500px !important; }
            x3d { width: 100% !important; height: 400px !important; }
        </style>
    </head>
    <body>
        <form action="index.jsp" accept-charset="utf-8">
            <label for="id">Compound/reaction ID:</label>
            <input type="text" id="id" name="id">
            <br>
            <label for="fomrat">Format:</label>
            <select id="format" name="format">
                <option value="mol">MOL</option>
                <option value="rxn">RXN</option>
            </select>
            <br>
            <button type="submit">View as X3D</button>
        </form>
        <jsp:include page="ctfile2x3d" flush="true">
            <jsp:param name="id" value="${param.id}"/>
            <jsp:param name="format" value="${param.format}"/>
        </jsp:include>
    </body>
</html>