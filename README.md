ctfile2x3d
==========

Utilities to convert MDL CTFiles into X3D.

Thid project was inspired by Asad's [atom-atom mapping tool](https://github.com/asad/AAMTool) which would allow me to create animations for biochemical reactions:
>Rahman, S.A. et.al.(2014) EC-BLAST: A Tool to Automatically Search and Compare Enzyme Reactions, Nature Methods.

**Directories and contents:**
* x3d: JAXB-generated java package for X3D schemas.
* ctfile2x3d-lib: main library with parsers for CTFiles.
* ctfile2x3d-webapp: sample web application delivering X3D content.

**Requirements:**
* Maven 3 (for building)
* Java 7
* Tomcat 6 (or other webapp container) and HTML5-capable browser (for webapp)
