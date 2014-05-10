<%@page contentType="model/x3d+xml" pageEncoding="UTF-8" isErrorPage="true"%>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<X3D>
    <Scene>
        <Transform translation="0.0 0.0 0.0">
            <Shape>
               <Appearance>
                   <Material diffuseColor="1 0 0"/>
               </Appearance>
               <Text string="ERROR: ${error}">
                   <FontStyle family="SANS" justify="MIDDLE" size="0.5"/>
               </Text>
           </Shape>
        </Transform>
    </Scene>
</X3D>
