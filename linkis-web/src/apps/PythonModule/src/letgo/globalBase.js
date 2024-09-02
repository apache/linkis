import { omit } from 'lodash-es';
import { letgoRequest } from './letgoRequest';

export class LetgoGlobalBase {
    constructor(globalCtx) {
        this._globalCtx = globalCtx;
        this.$request = letgoRequest;
    }

    get $utils() {
        return this._globalCtx.$utils;
    }

    get $context() {
        return this._globalCtx.$context;
    }

    get $globalCode() {
        return omit(this._globalCtx, '$utils', '$context');
    }
}
