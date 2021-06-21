import { HalResource, HalLink } from 'hal-types';

type HalCollectionV2<T extends HalResource<string, unknown> = HalResource<string, unknown>> = {
    total: number;
    _links: {
        first: HalLink,
        last: HalLink,
        prev: HalLink,
        next: HalLink;
    };
    _embedded: {
        item: HalResource[];
    };
} & T;