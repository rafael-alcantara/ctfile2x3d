<%@page pageEncoding="UTF-8" %>
<html>
    <head>
        <meta charset="utf-8">
        <link rel="stylesheet" href="x3dom-1.6.0/x3dom.css">
        <script src="x3dom-1.6.0/x3dom-full.js"></script>
        <style>
            x3d { width: 100% !important; height: 400px !important; }
            label, input, select, option, button {
                font-family: sans-serif;
                font-size: large;
            }
            button {
                font-weight: bold;
                background-color: turquoise;
                border-radius: 10px;
            }
            label, button {
                margin-left: 2em;
            }
        </style>
    </head>
    <body>
        <form action="index.jsp" accept-charset="utf-8">
            <label for="id">Compound/reaction ID:</label>
            <input type="text" id="id" name="id" value="${param.id}">
            <label for="format">Format:</label>
            <select id="format" name="format" >
                <option value="mol">MOL</option>
                <option value="rxn">RXN</option>
            </select>
            <button type="submit">View as X3D</button>
        </form>
        <div id="x3d">
            <jsp:include page="ctfile2x3d" flush="true">
                <jsp:param name="id" value="${param.id}"/>
                <jsp:param name="format" value="${param.format}"/>
            </jsp:include>
            <%--
            <X3D profile="Full">
                <Scene>
                    <Transform>
                        <Inline url="ctfile2x3d?id=${param.id}&amp;format=${param.format}"/>
                    </Transform>
                </Scene>
            </X3D>
            --%>
        </div>
        <a href="${ctfileURL}">${empty ctfileURL? '' : 'Download CTFile'}</a>
    </body>
</html>