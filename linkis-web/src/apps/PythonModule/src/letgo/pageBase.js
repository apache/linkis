import { LetgoGlobalBase } from './globalBase';

export class LetgoPageBase extends LetgoGlobalBase {
    constructor(ctx) {
        super(ctx.globalCtx);
        this.$pageCode = ctx.codes;
        this.$refs = ctx.instances;
    }
}
