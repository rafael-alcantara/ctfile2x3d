/**
 * Filters a transparency value, converting a boolean into a number if needed.
 * @param {type} transparency a transparency value as number or boolean.
 * @returns {Number} A numeric transparency value.
 */
function getTransparency(transparency){
    if (typeof transparency === 'boolean'){
        transparency = transparency? 1 : 0;
    }
    return transparency;
}

/**
 * Sets the transparency of materials.
 * @param {type} cssClass CSS class applied to Material elements.
 * @param {type} transparency the transparency (float or boolean).
 * @returns {undefined}
 */
function setTransparency(cssClass, transparency){
    var nodes = document.getElementsByClassName(cssClass);
    for (i = 0; i < nodes.length; i++){
        nodes[i].transparency = getTransparency(transparency);
    }
}

/**
 * Sets the scale of transforms.
 * @param {type} cssClass CSS class applied to Transform elements.
 * @param {type} scale the scale value (SFFloat), will be applied to all
 *      three dimensions.
 * @returns {undefined}
 */
function setScale(cssClass, scale){
    var nodes = document.getElementsByClassName(cssClass);
    for (i = 0; i < nodes.length; i++){
        nodes[i].scale = scale + ' ' + scale + ' ' + scale;
    }
}

/**
 * Sets the diffuse color of materials.
 * @param {type} cssClass cssClass CSS class applied to Material elements.
 * @param {type} color the color to apply (SFColor).
 * @returns {undefined}
 */
function setColor(cssClass, color){
    var nodes = document.getElementsByClassName(cssClass);
    for (i = 0; i < nodes.length; i++){
        nodes[i].diffuseColor = color;
    }
}

/**
 * Sets the radius of the bond cylinders.
 * @param {type} radius the new radius.
 * @returns {undefined}
 */
function setBondRadius(radius){
    var nodes = document.getElementsByClassName('BondCylinder');
    for (i = 0; i < nodes.length; i++){
        nodes[i].radius = radius;
    }
}

function showWireframe(){
    setTransparency('AtomSphereMaterial', true);
    setTransparency('AtomLabelMaterial', false);
    setTransparency('BondMaterial', false);
    setBondRadius(0.02);
    setScale('BondCylinderTransform', 0.5);
}

function showSticks(){
    setTransparency('AtomSphereMaterial', true);
    setTransparency('AtomLabelMaterial', true);
    setTransparency('BondMaterial', false);
    setBondRadius(0.05);
    setScale('BondCylinderTransform', 1);
}

function showBallsAndSticks(){
    setTransparency('AtomSphereMaterial', false);
    setScale('AtomSphereTransform', 0.5);
    setTransparency('AtomLabelMaterial', true);
    setTransparency('BondMaterial', false);
    setBondRadius(0.05);
    setScale('BondCylinderTransform', 1);
}

function showSpaceFill(){
    setTransparency('AtomSphereMaterial', false);
    setScale('AtomSphereTransform', 1);
    setTransparency('AtomLabelMaterial', true);
    setTransparency('BondMaterial', true);
}

function showMixed(){
    setTransparency('AtomSphereMaterial', 0.5);
    setScale('AtomSphereTransform', 1);
    setTransparency('AtomLabelMaterial', false);
    setTransparency('BondMaterial', false);
    setBondRadius(0.05);
    setScale('BondCylinderTransform', 0.5);
}

function setDisplay(display){
    switch (display){
        case 'WIREFRAME':
            showWireframe();
            break;
        case 'STICKS':
            showSticks();
            break;
        case 'BALLS_STICKS':
            showBallsAndSticks();
            break;
        case 'SPACEFILL':
            showSpaceFill();
            break;
        case 'MIXED':
            showMixed();
            break;
    }
}
