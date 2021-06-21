import { HalResource } from 'hal-types';

type ExampleResource<T extends HalResource<string, unknown> = HalResource<string, unknown>> = {
    description : string;
    complexAttribute : {
        name : string;
    };
} & T;