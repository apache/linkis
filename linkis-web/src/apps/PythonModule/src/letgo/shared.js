export function isGetterProp(instance, key) {
    if (key in instance) {
        let p = instance;
        let propDesc;
        while (p && !propDesc) {
            propDesc = Object.getOwnPropertyDescriptor(p, key);
            p = Object.getPrototypeOf(p);
        }
        // only getter
        return !!propDesc && propDesc.get && !propDesc.set;
    }
    return false;
}

export function getAllMethodAndProperties(obj) {
    let props = [];

    do {
        const l = Object.getOwnPropertyNames(obj)
            .concat(Object.getOwnPropertySymbols(obj).map((s) => s.toString()))
            .sort()
            .filter(
                (p, i, arr) =>
                    !['constructor', '_globalCtx'].includes(p) && // not the constructor
                    (i === 0 || p !== arr[i - 1]) && // not overriding in this prototype
                    !props.includes(p), // not overridden in a child
            );
        props = props.concat(l);
    } while (
        // eslint-disable-next-line no-cond-assign
        (obj = Object.getPrototypeOf(obj)) && // walk-up the prototype chain
        Object.getPrototypeOf(obj) // not the the Object prototype methods (hasOwnProperty, etc...)
    );

    return props.reverse();
}
