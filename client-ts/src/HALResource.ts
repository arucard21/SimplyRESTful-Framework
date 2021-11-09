import { HalLink } from 'hal-types';

export type HALResource<T extends Record<string, any> = Record<string, any>> = {
    _links?: {
        self?: HalLink;
    };
    _embedded?: {
        [rel: string]: HALResource;
    };
} & T;
