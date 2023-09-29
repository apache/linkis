export type CheckedRow = {
    taskID: string | number;
    instance: any;
    strongerExecId: number | string;
};

export type CheckedRows = Array<CheckedRow>;
